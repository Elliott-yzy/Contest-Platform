package com.w16a.danish.user.controller;

import cn.hutool.core.util.StrUtil;
import com.w16a.danish.user.config.FrontendProperties;
import com.w16a.danish.user.config.GithubOAuthProperties;
import com.w16a.danish.user.config.GoogleOAuthProperties;
import com.w16a.danish.user.domain.dto.*;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.feign.FileServiceClient;
import com.w16a.danish.user.service.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description User Management Controller
 */
@Tag(name = "User Management", description = "APIs for user account management including registration, login, profile updates, and account deletion.")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final IUsersService userService;
    private final GithubOAuthProperties githubOAuthProperties;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final FrontendProperties frontendProperties;
    private final FileServiceClient fileServiceClient;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and assigns a default role. Returns the user information with token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered",
                            content = @Content(schema = @Schema(implementation = UserResponseVO.class))
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerDTO) {
        UserResponseVO response = userService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "User login",
            description = "Authenticates a user using email and password, and returns user info with JWT token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials (email and password)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(schema = @Schema(implementation = UserResponseVO.class))
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDTO) {
        UserResponseVO response = userService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "User logout",
            description = "Invalidates the current JWT token by adding it to the Redis blacklist.",
            parameters = {
                    @Parameter(
                            name = HttpHeaders.AUTHORIZATION,
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "Bearer token used for authentication"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Logout successful"
                    )
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        userService.logout(token);

        return ResponseEntity.ok("Logout successful");
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a user account. ADMIN can delete any user, regular users can only delete themselves.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "ID of the user to be deleted",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "123e4567-e89b-12d3-a456-426614174000"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User deleted successfully"
                    )
            }
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId,
                                        @RequestHeader("User-ID") String currentUserId,
                                        @RequestHeader("User-Role") String currentUserRole) {
        String response = userService.deleteUserById(userId, currentUserId, currentUserRole);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get user profile",
            description = "Retrieves the profile details of the authenticated user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileVO.class))
                    )
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("User-ID") String currentUserId) {
        UserProfileVO userProfile = userService.getUserProfile(currentUserId);
        return ResponseEntity.ok(userProfile);
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user profile data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateUserDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile updated successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileVO.class))
                    )
            }
    )
    @PutMapping("/profile")
    public ResponseEntity<UserProfileVO> updateProfile(
            @RequestHeader("User-ID") String userId,
            @RequestBody UpdateUserDTO updateUserDTO) {

        UserProfileVO updated = userService.updateUserProfile(userId, updateUserDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Upload and update user avatar",
            description = "Uploads an avatar image to the file service and updates the user profile with the new avatar URL.",
            parameters = {
                    @Parameter(
                            name = "file",
                            description = "Avatar image file (JPG, PNG, etc.)",
                            required = true,
                            content = @Content(
                                    mediaType = "multipart/form-data",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Avatar uploaded and user profile updated",
                            content = @Content(schema = @Schema(implementation = UserProfileVO.class))
                    )
            }
    )
    @PostMapping("/profile/avatar")
    public ResponseEntity<UserProfileVO> uploadAndSetAvatar(
            @RequestHeader("User-ID") String userId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = fileServiceClient.uploadAvatar(file).getBody();
        UserProfileVO currentProfile = userService.getUserProfile(userId);
        String oldAvatarUrl = currentProfile.getAvatarUrl();
        if (StrUtil.isNotBlank(oldAvatarUrl)) {
            URI uri = URI.create(oldAvatarUrl);
            String[] parts = uri.getPath().substring(1).split("/", 2);
            if (parts.length == 2) {
                fileServiceClient.deleteFile(parts[0], parts[1]);
            }
        }
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setAvatarUrl(avatarUrl);
        UserProfileVO updated = userService.updateUserProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Redirect to GitHub OAuth page",
            description = "Redirects the user to GitHub's OAuth authorization page. The role (PARTICIPANT or ORGANIZER) will be passed as the state parameter.",
            parameters = {
                    @Parameter(
                            name = "role",
                            in = ParameterIn.QUERY,
                            required = true,
                            description = "User role for registration after GitHub login (PARTICIPANT or ORGANIZER)",
                            example = "PARTICIPANT"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "302", description = "Redirect to GitHub OAuth authorization page")
            }
    )
    @GetMapping("/oauth/github")
    public void redirectToGithubOauth(@RequestParam("role") String role, HttpServletResponse response) throws IOException {
        String redirectUrl = UriComponentsBuilder
                .fromUri(URI.create(githubOAuthProperties.getAuthorizeUrl()))
                .queryParam("client_id", githubOAuthProperties.getClientId())
                .queryParam("redirect_uri", githubOAuthProperties.getRedirectUri())
                .queryParam("scope", "user:email")
                .queryParam("state", role)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/oauth/callback/github")
    public void handleGithubCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String role,
            HttpServletResponse response
    ) throws IOException {
        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("github");
        dto.setCode(code);
        dto.setRole(role);

        UserResponseVO userInfo = userService.oauthLoginOrRegister(dto);
        String redirectUrl = frontendProperties.buildOauthRedirectUrl(
                userInfo.getAccessToken(),
                userInfo.getEmail(),
                userInfo.getRole(),
                userInfo.getUserId()
        );
        response.sendRedirect(redirectUrl);
    }

    @Operation(
            summary = "Redirect to Google OAuth page",
            description = "Redirects the user to Google OAuth authorization page. The `role` (PARTICIPANT or ORGANIZER) will be passed in the state parameter.",
            parameters = {
                    @Parameter(
                            name = "role",
                            in = ParameterIn.QUERY,
                            required = true,
                            description = "User role for registration after Google login (PARTICIPANT or ORGANIZER)",
                            example = "ORGANIZER"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "302", description = "Redirect to Google OAuth authorization page")
            }
    )
    @GetMapping("/oauth/google")
    public void redirectToGoogleOauth(@RequestParam("role") String role, HttpServletResponse response, HttpSession session) throws IOException {
        String state = StrUtil.uuid();
        session.setAttribute("oauth_state", state);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(googleOAuthProperties.getAuthorizeUrl())
                .queryParam("client_id", googleOAuthProperties.getClientId())
                .queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("include_granted_scopes", "true")
                .queryParam("state", state + ":" + role)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/oauth/callback/google")
    public void handleGoogleCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpSession session, HttpServletResponse response) throws IOException {
        String savedState = (String) session.getAttribute("oauth_state");
        String[] stateParts = state.split(":");
        String receivedState = stateParts[0];
        String role = stateParts.length > 1 ? stateParts[1] : "PARTICIPANT";

        if (!Objects.equals(receivedState, savedState)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid OAuth state");
            return;
        }

        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("google");
        dto.setCode(code);
        dto.setRole(role);

        UserResponseVO userInfo = userService.oauthLoginOrRegister(dto);
        String redirectUrl = frontendProperties.buildOauthRedirectUrl(
                userInfo.getAccessToken(),
                userInfo.getEmail(),
                userInfo.getRole(),
                userInfo.getUserId()
        );
        response.sendRedirect(redirectUrl);
    }

    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset link to the user's registered email. The email must be valid and registered in the system.",
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Registered email address of the user",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "user@example.com"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reset link sent (if the email exists)",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        userService.sendResetLink(email);
        return ResponseEntity.ok("If the email exists, a reset link has been sent.");
    }

    @Operation(
            summary = "Reset user password",
            description = "Validates the password reset token and sets the new password for the user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reset token and new password",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset successfully",
                            content = @Content(schema = @Schema(implementation = UserResponseVO.class))
                    )
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<UserResponseVO> resetPassword(@RequestBody ResetPasswordRequest request) {
        UserResponseVO response = userService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get user details by IDs with optional role filtering",
            description = "Returns a list of user brief info (ID, name, email, etc.) by a given list of user IDs. "
                    + "You can optionally filter users by role (e.g., PARTICIPANT, ORGANIZER).",
            parameters = {
                    @Parameter(
                            name = "role",
                            description = "Optional role to filter users by (e.g., PARTICIPANT, ORGANIZER)",
                            example = "PARTICIPANT"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of user IDs to query",
                    required = true,
                    content = @Content(schema = @Schema(implementation = String.class, type = "array", example = "[\"a7b3c5e7-d8fa-4d02-98b1-23cfaa72f456\"]"))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users matching the criteria",
                            content = @Content(schema = @Schema(implementation = UserBriefVO.class))
                    )
            }
    )
    @PostMapping("/query-by-ids")
    public ResponseEntity<List<UserBriefVO>> getUsersByIds(
            @RequestBody List<String> userIds,
            @RequestParam(required = false) String role
    ) {
        List<UserBriefVO> users = userService.getUsersByIds(userIds, role);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get basic user information by user ID",
            description = "Returns user brief info including name, email, avatar, and description.",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "User ID to query",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "a7b3c5e7-d8fa-4d02-98b1-23cfaa72f456"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found",
                            content = @Content(schema = @Schema(implementation = UserBriefVO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserBriefVO> getUserBriefById(@PathVariable String userId) {
        UserBriefVO user = userService.getUserBriefById(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Query users by emails",
            description = "Fetch basic user info by a list of emails. Used for assigning judges and other operations.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of user emails",
                    required = true,
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
            }
    )
    @PostMapping("/query-by-emails")
    public ResponseEntity<List<UserBriefVO>> getUsersByEmails(@RequestBody List<String> emails) {
        List<UserBriefVO> users = userService.getUsersByEmails(emails);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Admin: List all users with filters (paginated)",
            description = "ADMINs can view all users. Supports filtering by role and keyword (name or email), with pagination and sorting.",
            parameters = {
                    @Parameter(name = "role", description = "Optional role filter (e.g., PARTICIPANT, ORGANIZER, JUDGE, ADMIN)", required = false),
                    @Parameter(name = "keyword", description = "Optional keyword to search by name or email", required = false),
                    @Parameter(name = "page", description = "Page number (default: 1)", example = "1"),
                    @Parameter(name = "size", description = "Page size (default: 10)", example = "10"),
                    @Parameter(name = "sortBy", description = "Field to sort by (default: createdAt)", required = false),
                    @Parameter(name = "order", description = "Sort order (asc or desc)", example = "desc", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user list", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Only ADMINs can access this endpoint")
            }
    )
    @GetMapping("/admin/list")
    public ResponseEntity<PageResponse<AdminUserVO>> listAllUsersAdmin(
            @RequestHeader("User-ID") String adminId,
            @RequestHeader("User-Role") String adminRole,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        PageResponse<AdminUserVO> users = userService.listUsersAdmin(adminId, adminRole, role, keyword, page, size, sortBy, order);
        return ResponseEntity.ok(users);
    }

}

