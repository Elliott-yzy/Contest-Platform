package com.w16a.danish.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.w16a.danish.user.config.FrontendProperties;
import com.w16a.danish.user.config.GithubOAuthProperties;
import com.w16a.danish.user.config.JwtConfig;
import com.w16a.danish.user.domain.dto.*;
import com.w16a.danish.user.domain.po.Roles;
import com.w16a.danish.user.domain.po.UserRoles;
import com.w16a.danish.user.domain.po.Users;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.exception.BusinessException;
import com.w16a.danish.user.feign.*;
import com.w16a.danish.user.mapper.UsersMapper;
import com.w16a.danish.user.service.IRolesService;
import com.w16a.danish.user.service.IUserRolesService;
import com.w16a.danish.user.service.impl.UsersServiceImpl;
import com.w16a.danish.user.util.JwtUtil;
import com.w16a.danish.user.util.PasswordUtil;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsersServiceImpl covering all core functionalities.
 */
class UsersServiceImplTest {

    @Spy
    @InjectMocks
    private UsersServiceImpl usersService;

    @Mock private UsersMapper usersMapper;
    @Mock private IRolesService rolesService;
    @Mock private IUserRolesService userRolesService;
    @Mock private JwtConfig jwtConfig;
    @Mock private PasswordUtil passwordUtil;
    @Mock private JwtUtil jwtUtil;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private JavaMailSender mailSender;
    @Mock private GithubOAuthClient githubOAuthClient;
    @Mock private GithubUserClient githubUserClient;
    @Mock private FrontendProperties frontendProperties;
    @Mock
    private GithubOAuthProperties githubOAuthProperties;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtConfig.getSecret()).thenReturn("mocked-secret");
        when(jwtConfig.getExpiration()).thenReturn(3600000L);

        java.lang.reflect.Field baseMapperField = UsersServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(usersService, usersMapper);

        // Default mock for lambdaQuery
        LambdaQueryChainWrapper<Users> userQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(userQuery).when(usersService).lambdaQuery();
        when(userQuery.eq(any(), any())).thenReturn(userQuery);
        when(userQuery.exists()).thenReturn(false);
        when(userQuery.one()).thenReturn(null);
    }

    @Test
    @DisplayName("✅ Should register successfully")
    void testRegisterSuccess() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("Password1");
        dto.setRole("PARTICIPANT");

        LambdaQueryChainWrapper<Roles> roleQuery = mock(LambdaQueryChainWrapper.class);
        when(rolesService.lambdaQuery()).thenReturn(roleQuery);
        when(roleQuery.eq(any(), any())).thenReturn(roleQuery);
        when(roleQuery.one()).thenReturn(new Roles().setId(1).setName("PARTICIPANT"));

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(true);
        when(passwordUtil.encryptPassword(anyString())).thenReturn("encryptedPassword");
        when(jwtUtil.generateAndStoreToken(anyMap(), anyString(), anyLong())).thenReturn("jwt-token");

        UserResponseVO result = usersService.register(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("✅ Should login successfully")
    void testLoginSuccess() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("Password1");
        dto.setRole("PARTICIPANT");

        Users mockUser = new Users().setId("uid").setEmail(dto.getEmail()).setPassword("hashed");
        doReturn(mockUser).when(usersService).getOne(any(LambdaQueryWrapper.class));

        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(userRolesService.getOne(any())).thenReturn(new UserRoles().setRoleId(1));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setId(1).setName("PARTICIPANT"));
        when(jwtUtil.generateAndStoreToken(anyMap(), anyString(), anyLong())).thenReturn("jwt-token");

        UserResponseVO result = usersService.login(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("❌ Should fail login with invalid credentials")
    void testLoginFail() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("wrong@test.com");
        dto.setPassword("wrongpass");
        dto.setRole("PARTICIPANT");

        doReturn(null).when(usersService).getOne(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> usersService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("✅ Should oauth login/register successfully (GitHub)")
    void testOauthLoginOrRegister_GithubSuccess() {
        // Arrange
        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("github");
        dto.setCode("mock-code");
        dto.setRole("PARTICIPANT");

        // Mock GitHub OAuth Properties
        when(githubOAuthProperties.getClientId()).thenReturn("mock-client-id");
        when(githubOAuthProperties.getClientSecret()).thenReturn("mock-client-secret");
        when(githubOAuthProperties.getRedirectUri()).thenReturn("http://mock-redirect-uri");

        // Mock GitHub OAuth Client to return access_token
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "mocked-token");
        when(githubOAuthClient.getAccessToken(any())).thenReturn(tokenResponse);

        // Mock GitHub User Info Client
        GithubUserDTO mockGithubUser = new GithubUserDTO();
        mockGithubUser.setLogin("mockuser");
        mockGithubUser.setEmail("mockuser@example.com");
        when(githubUserClient.getUserInfo(anyString())).thenReturn(mockGithubUser);

        // Mock lambdaQuery for checking existing user (none found)
        LambdaQueryChainWrapper<Users> userQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(userQuery).when(usersService).lambdaQuery();
        when(userQuery.eq(any(), any())).thenReturn(userQuery);
        when(userQuery.one()).thenReturn(null); // Means user not found
        when(userQuery.exists()).thenReturn(false);

        // Mock lambdaQuery for roles
        LambdaQueryChainWrapper<Roles> roleQuery = mock(LambdaQueryChainWrapper.class);
        when(rolesService.lambdaQuery()).thenReturn(roleQuery);
        when(roleQuery.eq(any(), any())).thenReturn(roleQuery);
        when(roleQuery.one()).thenReturn(new Roles().setId(1).setName("PARTICIPANT"));

        // Mock random password encryption
        when(passwordUtil.encryptPassword(anyString())).thenReturn("encryptedRandomPass");

        // Mock JWT generation
        when(jwtUtil.generateAndStoreToken(anyMap(), anyString(), anyLong())).thenReturn("jwt-token");

        // Act
        UserResponseVO result = usersService.oauthLoginOrRegister(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }


    @Test
    @DisplayName("✅ Should reset password successfully")
    void testResetPasswordSuccess() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("Password1New");

        when(valueOperations.get(anyString())).thenReturn("userId");
        Users user = new Users().setPassword("oldPass");
        when(usersService.getById(anyString())).thenReturn(user);

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(true);
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(false);
        when(jwtUtil.generateAndStoreToken(anyMap(), anyString(), anyLong())).thenReturn("new-jwt-token");
        when(userRolesService.getOne(any())).thenReturn(new UserRoles().setRoleId(1));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setName("PARTICIPANT"));

        UserResponseVO result = usersService.resetPassword(request);

        assertThat(result.getAccessToken()).isEqualTo("new-jwt-token");
    }

    @Test
    @DisplayName("✅ Should send reset link successfully")
    void testSendResetLinkSuccess() {
        Users user = new Users().setId("userId");

        LambdaQueryChainWrapper<Users> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(usersService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.one()).thenReturn(user);

        MimeMessage fakeMimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(frontendProperties.buildResetPasswordUrl(anyString())).thenReturn("http://mocked-url/reset");
        when(mailSender.createMimeMessage()).thenReturn(fakeMimeMessage);

        usersService.sendResetLink("test@example.com");

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("✅ Should logout successfully")
    void testLogoutSuccess() {
        usersService.logout("some-token");

        verify(jwtUtil).blacklistToken(eq("some-token"), anyLong());
    }

    @Test
    @DisplayName("✅ Should delete user successfully (Admin privilege)")
    void testDeleteUserById_AdminSuccess() {
        Users user = new Users().setId("uid").setEmail("email@test.com");

        when(usersService.getById(anyString())).thenReturn(user);
        when(usersService.removeById(anyString())).thenReturn(true);

        String result = usersService.deleteUserById("uid", "adminUid", "ADMIN");

        assertThat(result).isEqualTo("User deleted successfully");
    }

    @Test
    @DisplayName("❌ Should not allow non-admin to delete others")
    void testDeleteUserById_Forbidden() {
        Users user = new Users().setId("uid");

        when(usersService.getById(anyString())).thenReturn(user);

        assertThatThrownBy(() -> usersService.deleteUserById("uid", "otherUid", "PARTICIPANT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("✅ Should update user profile successfully")
    void testUpdateUserProfileSuccess() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setDescription("New description");

        Users user = new Users().setId("uid").setEmail("test@test.com");

        when(usersService.getById(anyString())).thenReturn(user);
        when(usersService.updateById(any())).thenReturn(true);

        UserProfileVO profile = usersService.updateUserProfile("uid", dto);

        assertThat(profile).isNotNull();
    }

    @Test
    @DisplayName("✅ Should get users by IDs successfully")
    void testGetUsersByIdsSuccess() {
        List<String> ids = Arrays.asList("uid1", "uid2");

        LambdaQueryChainWrapper<Users> userQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(userQuery).when(usersService).lambdaQuery();

        when(userQuery.in(any(), anyCollection())).thenReturn(userQuery);

        List<Users> mockUsers = List.of(
                new Users().setId("uid1").setName("User One").setEmail("user1@example.com"),
                new Users().setId("uid2").setName("User Two").setEmail("user2@example.com")
        );
        when(userQuery.list()).thenReturn(mockUsers);

        // mock userRolesService.list()
        when(userRolesService.list(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(new UserRoles().setUserId("uid1").setRoleId(1)));

        // mock rolesService.getById()
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setId(1).setName("PARTICIPANT"));

        // Act
        List<UserBriefVO> result = usersService.getUsersByIds(ids, "PARTICIPANT");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("uid1");
    }


    @Test
    @DisplayName("✅ Should get user brief by ID successfully")
    void testGetUserBriefByIdSuccess() {
        Users user = new Users().setId("uid").setName("Test User");

        when(usersService.getById(anyString())).thenReturn(user);

        UserBriefVO result = usersService.getUserBriefById("uid");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("✅ Should get users by emails successfully")
    void testGetUsersByEmailsSuccess() {
        List<String> emails = List.of("test@test.com");

        LambdaQueryChainWrapper<Users> userQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(userQuery).when(usersService).lambdaQuery();

        when(userQuery.in(any(), anyCollection())).thenReturn(userQuery);

        List<Users> mockUsers = List.of(
                new Users().setId("user1").setName("Test User").setEmail("test@test.com")
        );
        when(userQuery.list()).thenReturn(mockUsers);

        // Act
        List<UserBriefVO> result = usersService.getUsersByEmails(emails);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("✅ Should list admin users successfully")
    void testListUsersAdminSuccess() {
        when(usersService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class));
        when(userRolesService.list()).thenReturn(List.of(new UserRoles().setUserId("uid").setRoleId(1)));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setName("PARTICIPANT"));

        PageResponse<AdminUserVO> page = usersService.listUsersAdmin("adminId", "ADMIN", null, null, 1, 10, "createdAt", "asc");

        assertThat(page).isNotNull();
    }

    @Test
    @DisplayName("❌ Should fail register with invalid email")
    void testRegisterFail_InvalidEmail() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("Password1");
        dto.setRole("PARTICIPANT");

        assertThatThrownBy(() -> usersService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("❌ Should fail register with weak password")
    void testRegisterFail_WeakPassword() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("weak");
        dto.setRole("PARTICIPANT");

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> usersService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password must be at least 8 characters");
    }

    @Test
    @DisplayName("❌ Should fail register with duplicate email")
    void testRegisterFail_DuplicateEmail() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("Password1");
        dto.setRole("PARTICIPANT");

        LambdaQueryChainWrapper<Users> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(usersService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        assertThatThrownBy(() -> usersService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("❌ Should fail register with invalid role")
    void testRegisterFail_InvalidRole() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("Password1");
        dto.setRole("INVALID");

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(true);
        LambdaQueryChainWrapper<Roles> roleQuery = mock(LambdaQueryChainWrapper.class);
        when(rolesService.lambdaQuery()).thenReturn(roleQuery);
        when(roleQuery.eq(any(), any())).thenReturn(roleQuery);
        when(roleQuery.one()).thenReturn(null);

        assertThatThrownBy(() -> usersService.register(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    @DisplayName("❌ Should fail login with role mismatch")
    void testLoginFail_RoleMismatch() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("Password1");
        dto.setRole("ORGANIZER");

        Users mockUser = new Users().setId("uid").setEmail(dto.getEmail()).setPassword("hashed");
        doReturn(mockUser).when(usersService).getOne(any(LambdaQueryWrapper.class));

        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(userRolesService.getOne(any())).thenReturn(new UserRoles().setRoleId(1));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setId(1).setName("PARTICIPANT"));

        assertThatThrownBy(() -> usersService.login(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Selected role does not match your account role");
    }

    @Test
    @DisplayName("❌ Should fail oauth login with unsupported provider")
    void testOauthLoginOrRegister_UnsupportedProvider() {
        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("facebook");
        dto.setCode("code");
        dto.setRole("PARTICIPANT");

        assertThatThrownBy(() -> usersService.oauthLoginOrRegister(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported provider");
    }

    @Test
    @DisplayName("❌ Should fail reset password with invalid token")
    void testResetPasswordFail_InvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setNewPassword("Password1New");

        when(valueOperations.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> usersService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Reset link is invalid or expired");
    }

    @Test
    @DisplayName("❌ Should fail reset password with invalid new password")
    void testResetPasswordFail_WeakNewPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("weak");

        when(valueOperations.get(anyString())).thenReturn("userId");
        when(usersService.getById(anyString())).thenReturn(new Users());

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(false);

        assertThatThrownBy(() -> usersService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password must be at least 8 characters");
    }

    @Test
    @DisplayName("❌ Should fail reset password if same as old password")
    void testResetPasswordFail_SameAsOld() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("Password1New");

        when(valueOperations.get(anyString())).thenReturn("userId");
        Users user = new Users().setPassword("hashedPassword");
        when(usersService.getById(anyString())).thenReturn(user);

        when(passwordUtil.isPasswordValid(anyString())).thenReturn(true);
        when(passwordUtil.verifyPassword(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> usersService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("New password must be different");
    }

    @Test
    @DisplayName("❌ Should fail update profile with invalid email")
    void testUpdateProfileFail_InvalidEmail() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("invalid");

        when(usersService.getById(anyString())).thenReturn(new Users());

        assertThatThrownBy(() -> usersService.updateUserProfile("uid", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("❌ Should fail update profile with duplicate email")
    void testUpdateProfileFail_EmailExists() {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("new@example.com");

        Users user = new Users().setEmail("old@example.com");

        when(usersService.getById(anyString())).thenReturn(user);

        LambdaQueryChainWrapper<Users> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(usersService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        assertThatThrownBy(() -> usersService.updateUserProfile("uid", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email is already in use");
    }

    @Test
    @DisplayName("❌ Should fail delete user if removeById fails")
    void testDeleteUserById_FailDelete() {
        Users user = new Users().setId("uid");

        when(usersService.getById(anyString())).thenReturn(user);
        when(usersService.removeById(anyString())).thenReturn(false);

        assertThatThrownBy(() -> usersService.deleteUserById("uid", "adminUid", "ADMIN"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to delete user");
    }

    // === Additional Coverage ===

    @Test
    @DisplayName("✅ Should get user profile successfully")
    void testGetUserProfileSuccess() {
        Users mockUser = new Users()
                .setId("uid")
                .setName("Test User")
                .setEmail("test@example.com")
                .setDescription("Hello");

        when(usersService.getById(anyString())).thenReturn(mockUser);

        UserProfileVO profile = usersService.getUserProfile("uid");

        assertThat(profile).isNotNull();
        assertThat(profile.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("✅ Should list users admin with empty filters")
    void testListUsersAdmin_EmptyFilters() {
        when(usersService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class));
        when(userRolesService.list()).thenReturn(List.of(new UserRoles().setUserId("uid").setRoleId(1)));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setName("PARTICIPANT"));

        PageResponse<AdminUserVO> page = usersService.listUsersAdmin("adminId", "ADMIN", null, null, 1, 10, null, null);

        assertThat(page).isNotNull();
    }

    @Test
    @DisplayName("✅ Should list users admin sorted descending")
    void testListUsersAdmin_SortDesc() {
        when(usersService.lambdaQuery()).thenReturn(mock(LambdaQueryChainWrapper.class));
        when(userRolesService.list()).thenReturn(List.of(new UserRoles().setUserId("uid").setRoleId(1)));
        when(rolesService.getById(anyInt())).thenReturn(new Roles().setName("PARTICIPANT"));

        PageResponse<AdminUserVO> page = usersService.listUsersAdmin("adminId", "ADMIN", null, null, 1, 10, "createdAt", "desc");

        assertThat(page).isNotNull();
    }

    @Test
    @DisplayName("✅ Should return empty when user not found in getUserProfile")
    void testGetUserProfile_UserNotFound() {
        when(usersService.getById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> usersService.getUserProfile("uid"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }

}
