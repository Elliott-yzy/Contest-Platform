package com.w16a.danish.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.w16a.danish.user.config.FrontendProperties;
import com.w16a.danish.user.config.GithubOAuthProperties;
import com.w16a.danish.user.config.GoogleOAuthProperties;
import com.w16a.danish.user.config.JwtConfig;
import com.w16a.danish.user.domain.dto.*;
import com.w16a.danish.user.domain.po.Roles;
import com.w16a.danish.user.domain.po.UserRoles;
import com.w16a.danish.user.domain.po.Users;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.exception.*;
import com.w16a.danish.user.feign.*;
import com.w16a.danish.user.mapper.UsersMapper;
import com.w16a.danish.user.service.IRolesService;
import com.w16a.danish.user.service.IUserRolesService;
import com.w16a.danish.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.user.util.JwtUtil;
import com.w16a.danish.user.util.PasswordUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description UsersServiceImpl
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final IRolesService rolesService;
    private final IUserRolesService userRolesService;
    private final JwtConfig jwtConfig;
    private final PasswordUtil passwordUtil;
    private final GithubOAuthProperties githubOAuthProperties;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final GithubOAuthClient githubOAuthClient;
    private final GithubUserClient githubUserClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final GoogleUserClient googleUserClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final FrontendProperties frontendProperties;
    private final FileServiceClient fileServiceClient;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;


    private static final long RESET_LINK_EXPIRATION_MINUTES = 15;


    @Override
    @Transactional
    public UserResponseVO register(RegisterRequestDTO registerDTO) {
        String email = registerDTO.getEmail();
        String password = registerDTO.getPassword();
        String roleName = registerDTO.getRole();

        if (!ReUtil.isMatch(RegexPool.EMAIL, email)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        boolean exists = lambdaQuery().eq(Users::getEmail, email).exists();
        if (exists) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (!passwordUtil.isPasswordValid(password)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters, include a number and an uppercase letter");
        }

        // create user object
        Users user = BeanUtil.copyProperties(registerDTO, Users.class);
        user.setId(StrUtil.uuid());
        user.setPassword(passwordUtil.encryptPassword(password));
        this.save(user);

        // assign role
        Roles role = rolesService.lambdaQuery().eq(Roles::getName, roleName).one();

        if (role == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }

        UserRoles userRole = new UserRoles();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRolesService.save(userRole);

        String token = jwtUtil.generateAndStoreToken(createClaims(user.getId(), role.getName()), jwtConfig.getSecret(), jwtConfig.getExpiration());
        return new UserResponseVO(user.getId(), user.getName(), user.getEmail(), role.getName(), token, jwtConfig.getExpiration() / 1000);
    }

    @Override
    @Transactional
    public UserResponseVO login(LoginRequestDTO loginDTO) {
        // find user by email
        Users user = getOne(
                new LambdaQueryWrapper<Users>()
                        .eq(Users::getEmail, loginDTO.getEmail())
        );

        if (user == null || !passwordUtil.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // get user role
        UserRoles userRole = userRolesService.getOne(
                new LambdaQueryWrapper<UserRoles>()
                        .eq(UserRoles::getUserId, user.getId())
        );

        if (userRole == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No permission to perform this action");
        }

        // get role
        Roles role = rolesService.getById(userRole.getRoleId());

        if (role == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User role is invalid or missing");
        }

        String inputRole = loginDTO.getRole().trim().toUpperCase();
        String dbRole = role.getName().trim().toUpperCase();

        if (!inputRole.equals(dbRole)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Selected role does not match your account role. Please choose the correct role.");
        }

        String token = jwtUtil.generateAndStoreToken(createClaims(user.getId(), role.getName()), jwtConfig.getSecret(), jwtConfig.getExpiration());
        return new UserResponseVO(user.getId(), user.getName(), user.getEmail(), role.getName(), token, jwtConfig.getExpiration() / 1000);
    }

    @Override
    @Transactional
    public String deleteUserById(String userId, String currentUserId, String currentUserRole) {
        // check if user exists
        Users user = getById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }

        // check if user has permission to delete
        // only ADMIN can delete other users
        boolean isSelf = currentUserId.equals(userId);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUserRole);
        if (!isSelf && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You do not have permission to delete this user");
        }

        if (StrUtil.isNotBlank(user.getAvatarUrl())) {
            URI uri = URI.create(user.getAvatarUrl());
            String[] parts = uri.getPath().substring(1).split("/", 2);
            if (parts.length == 2) {
                fileServiceClient.deleteFile(parts[0], parts[1]);
            }
        }

        // delete user roles
        userRolesService.remove(new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getUserId, userId));

        boolean userRemoved = removeById(userId);
        if (!userRemoved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user");
        }

        return "User deleted successfully";
    }

    @Override
    public UserProfileVO getUserProfile(String userId) {
        // Retrieve user by ID
        Users user = getById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }

        return BeanUtil.copyProperties(user, UserProfileVO.class);
    }

    @Override
    @Transactional
    public UserResponseVO oauthLoginOrRegister(OAuthLoginRequestDTO oAuthLoginRequestDTO) {
        String provider = oAuthLoginRequestDTO.getProvider().toLowerCase();
        String email, name;

        if ("google".equals(provider)) {
            MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
            tokenBody.add("client_id", googleOAuthProperties.getClientId());
            tokenBody.add("client_secret", googleOAuthProperties.getClientSecret());
            tokenBody.add("code", oAuthLoginRequestDTO.getCode());
            tokenBody.add("grant_type", "authorization_code");
            tokenBody.add("redirect_uri", googleOAuthProperties.getRedirectUri());

            Map<String, Object> tokenResponse = googleOAuthClient.getAccessToken(tokenBody);
            String accessToken = (String) tokenResponse.get("access_token");

            if (accessToken == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Failed to get Google access token");
            }

            GoogleUserDTO googleUser = googleUserClient.getUserInfo("Bearer " + accessToken);
            if (googleUser == null || googleUser.getEmail() == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Failed to get Google user info");
            }

            email = googleUser.getEmail();
            name = googleUser.getName();

        } else if ("github".equals(provider)) {
            Map<String, String> body = new HashMap<>();
            body.put("client_id", githubOAuthProperties.getClientId());
            body.put("client_secret", githubOAuthProperties.getClientSecret());
            body.put("code", oAuthLoginRequestDTO.getCode());
            body.put("redirect_uri", githubOAuthProperties.getRedirectUri());

            Map<String, Object> tokenResponse = githubOAuthClient.getAccessToken(body);
            String accessToken = (String) tokenResponse.get("access_token");

            if (accessToken == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Failed to retrieve access token from GitHub");
            }

            GithubUserDTO githubUser = githubUserClient.getUserInfo("Bearer " + accessToken);
            if (githubUser == null || githubUser.getLogin() == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Failed to retrieve GitHub user info");
            }

            email = githubUser.getEmail() != null ? githubUser.getEmail() : githubUser.getLogin() + "@github.com";
            name = githubUser.getLogin();
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        }

        String roleName = StringUtils.hasText(oAuthLoginRequestDTO.getRole()) ? oAuthLoginRequestDTO.getRole().toUpperCase() : "PARTICIPANT";
        if (!List.of("PARTICIPANT", "ORGANIZER").contains(roleName)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }

        Users user = this.lambdaQuery().eq(Users::getEmail, email).one();
        Roles role;

        if (user == null) {
            if (!ReUtil.isMatch(RegexPool.EMAIL, email)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid email format returned from " + provider);
            }

            boolean emailExists = this.lambdaQuery().eq(Users::getEmail, email).exists();
            if (emailExists) {
                throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered");
            }

            user = new Users();
            user.setId(StrUtil.uuid());
            user.setEmail(email);
            user.setName(name);
            user.setPassword(passwordUtil.encryptPassword(StrUtil.uuid()));
            this.save(user);

            role = rolesService.lambdaQuery().eq(Roles::getName, roleName).one();
            if (role == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName);
            }

            UserRoles userRole = new UserRoles();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRolesService.save(userRole);
        } else {
            UserRoles userRole = userRolesService.getOne(
                    new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getUserId, user.getId()));
            if (userRole == null) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "User has no role");
            }
            role = rolesService.getById(userRole.getRoleId());
        }

        String token = jwtUtil.generateAndStoreToken(createClaims(user.getId(), role.getName()), jwtConfig.getSecret(), jwtConfig.getExpiration());
        return new UserResponseVO(user.getId(), user.getName(), user.getEmail(), role.getName(), token, jwtConfig.getExpiration() / 1000);
    }

    @Override
    @Transactional
    public UserResponseVO resetPassword(ResetPasswordRequest request) {
        String redisKey = "reset:token:" + request.getToken();
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reset link is invalid or expired");
        }

        String newPassword = request.getNewPassword();
        if (!passwordUtil.isPasswordValid(newPassword)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters, contain a number and an uppercase letter");
        }

        Users user = this.getById(userId);

        if (passwordUtil.verifyPassword(newPassword, user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "New password must be different from the old one");
        }

        user.setPassword(passwordUtil.encryptPassword(newPassword));
        user.setUpdatedAt(null);
        this.updateById(user);

        redisTemplate.delete(redisKey);

        // get user role and create token
        UserRoles userRole = userRolesService.getOne(
                new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getUserId, user.getId())
        );
        if (userRole == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "User has no role");
        }

        Roles role = rolesService.getById(userRole.getRoleId());
        String token = jwtUtil.generateAndStoreToken(createClaims(user.getId(), role.getName()), jwtConfig.getSecret(), jwtConfig.getExpiration());
        return new UserResponseVO(user.getId(), user.getName(), user.getEmail(), role.getName(), token, jwtConfig.getExpiration() / 1000);
    }

    @Override
    @Transactional
    public void sendResetLink(String email) {
        // find user by email
        Users user = this.lambdaQuery().eq(Users::getEmail, email).one();
        if (user == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User not found");
        }

        // create reset token and save to Redis
        String token = StrUtil.uuid();
        String redisKey = "reset:token:" + token;
        redisTemplate.opsForValue().set(redisKey, user.getId(), RESET_LINK_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // send HTML email
        sendResetPasswordHtmlEmail(email, token);
    }

    @Override
    public void logout(String token) {
        jwtUtil.blacklistToken(token, jwtConfig.getExpiration());
    }

    @Override
    @Transactional
    public UserProfileVO updateUserProfile(String userId, UpdateUserDTO updateUserDTO) {
        // check if user exists
        Users user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }

        String newEmail = updateUserDTO.getEmail();
        if (StrUtil.isNotBlank(newEmail) && !StrUtil.equals(user.getEmail(), newEmail)) {
            if (!ReUtil.isMatch(RegexPool.EMAIL, newEmail)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid email format");
            }

            boolean exists = lambdaQuery().eq(Users::getEmail, newEmail).exists();
            if (exists) {
                throw new BusinessException(HttpStatus.CONFLICT, "Email is already in use");
            }

            user.setEmail(newEmail);
        }

        String newPassword = updateUserDTO.getPassword();
        if (StrUtil.isNotBlank(newPassword)) {
            if (!passwordUtil.isPasswordValid(newPassword)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long and contain at least one uppercase letter and one number");
            }

            if (passwordUtil.verifyPassword(newPassword, user.getPassword())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "New password must be different from the old one");
            }

            user.setPassword(passwordUtil.encryptPassword(newPassword));
        }

        BeanUtil.copyProperties(updateUserDTO, user,
                CopyOptions.create()
                        .ignoreNullValue()
                        .setIgnoreProperties("email", "password"));

        user.setUpdatedAt(null);
        if (!this.updateById(user)) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user profile");
        }

        return BeanUtil.copyProperties(user, UserProfileVO.class);
    }

    @Override
    public List<UserBriefVO> getUsersByIds(List<String> userIds, String role) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        List<Users> users = this.lambdaQuery()
                .in(Users::getId, userIds)
                .list();

        if (StrUtil.isNotBlank(role)) {
            Map<String, String> userIdToRoleMap = userRolesService.list(
                            new LambdaQueryWrapper<UserRoles>().in(UserRoles::getUserId, userIds))
                    .stream()
                    .collect(Collectors.toMap(
                            UserRoles::getUserId,
                            ur -> rolesService.getById(ur.getRoleId()).getName(),
                            (existing, replacement) -> existing
                    ));

            users = users.stream()
                    .filter(u -> role.equalsIgnoreCase(userIdToRoleMap.get(u.getId())))
                    .toList();
        }

        return users.stream()
                .map(user -> UserBriefVO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .description(user.getDescription())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public UserBriefVO getUserBriefById(String userId) {
        Users user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }

        return UserBriefVO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .description(user.getDescription())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public List<UserBriefVO> getUsersByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return List.of();
        }

        List<Users> users = this.lambdaQuery()
                .in(Users::getEmail, emails)
                .list();

        return users.stream()
                .map(user -> {
                    UserBriefVO vo = new UserBriefVO();
                    vo.setId(user.getId());
                    vo.setName(user.getName());
                    vo.setEmail(user.getEmail());
                    vo.setAvatarUrl(user.getAvatarUrl());
                    vo.setDescription(user.getDescription());
                    vo.setCreatedAt(user.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<AdminUserVO> listUsersAdmin(String adminId, String adminRole, String role, String keyword, int page, int size, String sortBy, String order) {
        if (!"ADMIN".equalsIgnoreCase(adminRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only ADMINs can access this resource.");
        }

        List<Users> usersList = this.lambdaQuery().list();
        if (CollUtil.isEmpty(usersList)) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        Map<String, String> userIdToRoleMap = userRolesService.list()
                .stream()
                .collect(Collectors.toMap(
                        UserRoles::getUserId,
                        ur -> Optional.ofNullable(rolesService.getById(ur.getRoleId()))
                                .map(Roles::getName)
                                .orElse("UNKNOWN"),
                        (existing, replacement) -> existing
                ));

        List<AdminUserVO> adminUsers = usersList.stream()
                .map(user -> AdminUserVO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .description(user.getDescription())
                        .createdAt(user.getCreatedAt())
                        .role(userIdToRoleMap.getOrDefault(user.getId(), "UNKNOWN"))
                        .build())
                .toList();

        if (StrUtil.isNotBlank(role)) {
            adminUsers = adminUsers.stream()
                    .filter(u -> role.equalsIgnoreCase(u.getRole()))
                    .toList();
        }

        if (StrUtil.isNotBlank(keyword)) {
            adminUsers = adminUsers.stream()
                    .filter(u -> StrUtil.containsIgnoreCase(u.getName(), keyword)
                            || StrUtil.containsIgnoreCase(u.getEmail(), keyword))
                    .toList();
        }

        Comparator<AdminUserVO> comparator = switch (sortBy) {
            case "name" -> Comparator.comparing(AdminUserVO::getName, String.CASE_INSENSITIVE_ORDER);
            case "email" -> Comparator.comparing(AdminUserVO::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "createdAt" -> Comparator.comparing(AdminUserVO::getCreatedAt);
            default -> Comparator.comparing(AdminUserVO::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        adminUsers = adminUsers.stream().sorted(comparator).toList();

        int total = adminUsers.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<AdminUserVO> pagedList = adminUsers.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }



    private void sendResetPasswordHtmlEmail(String email, String token) {
        try {
            String resetUrl = frontendProperties.buildResetPasswordUrl(token);
            String subject = "[Contest Platform] Password Reset Request";

            String content = String.format("""
            <div style="font-family:Arial,sans-serif;line-height:1.6;color:#333;">
              <p>Hello,</p>

              <p>We received a request to reset your password.</p>

              <p>
                Click the link below to set a new password (valid for <b>%d minutes</b>):<br>
                👉 <a href="%s" style="color:#1a73e8;text-decoration:none;">Click here to reset your password</a>
              </p>

              <p>If you did not request this, please ignore this email.</p>

              <br>
              <p>Regards,<br><b>Danish Competition Platform</b></p>
            </div>
            """, RESET_LINK_EXPIRATION_MINUTES, resetUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }

    private Map<String, Object> createClaims(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("exp", (System.currentTimeMillis() + jwtConfig.getExpiration()) / 1000);
        return claims;
    }

}
