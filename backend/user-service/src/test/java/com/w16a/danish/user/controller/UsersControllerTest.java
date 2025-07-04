package com.w16a.danish.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.user.config.FrontendProperties;
import com.w16a.danish.user.config.GithubOAuthProperties;
import com.w16a.danish.user.config.GoogleOAuthProperties;
import com.w16a.danish.user.domain.dto.*;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.feign.FileServiceClient;
import com.w16a.danish.user.service.IUsersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsersControllerTest {

    @TestConfiguration
    static class MockRabbitConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        public RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory() {
            return invocation -> mock(SimpleMessageListenerContainer.class);
        }

        @Bean
        public GoogleOAuthProperties googleOAuthProperties() {
            GoogleOAuthProperties properties = new GoogleOAuthProperties();
            properties.setClientId("fake-client-id");
            properties.setClientSecret("fake-client-secret");
            properties.setRedirectUri("http://localhost:8080/users/oauth/callback/google");
            properties.setAuthorizeUrl("https://accounts.google.com/o/oauth2/auth");
            properties.setTokenUrl("https://oauth2.googleapis.com/token");
            properties.setUserInfoUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            return properties;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FrontendProperties frontendProperties;

    @Autowired
    private GithubOAuthProperties githubOAuthProperties;

    @Autowired
    private GoogleOAuthProperties googleOAuthProperties;

    @MockitoBean
    private IUsersService userService;

    @MockitoBean
    private FileServiceClient fileServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("✅ Should register user successfully")
    void testRegisterSuccess() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setName("Test User");
        registerRequestDTO.setEmail("test@example.com");
        registerRequestDTO.setPassword("password123");
        registerRequestDTO.setRole("PARTICIPANT");

        UserResponseVO mockResponse = new UserResponseVO("123e4567-e89b-12d3-a456-426614174000", "Test User", "test@example.com", "PARTICIPANT", "mocked-token", 3600L);

        when(userService.register(any(RegisterRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("PARTICIPANT"))
                .andExpect(jsonPath("$.accessToken").value("mocked-token"));
    }

    @Test
    @DisplayName("✅ Should login user successfully")
    void testLoginSuccess() throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("test@example.com");
        loginRequestDTO.setPassword("password123");

        UserResponseVO mockResponse = new UserResponseVO(
                "123e4567-e89b-12d3-a456-426614174000",
                "Test User",
                "test@example.com",
                "PARTICIPANT",
                "jwt-token",
                3600L
        );

        when(userService.login(any(LoginRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    @DisplayName("✅ Should logout user successfully")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/users/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));

        verify(userService, times(1)).logout("valid-token");
    }

    @Test
    @DisplayName("✅ Should get user profile successfully")
    void testGetUserProfile() throws Exception {
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setName("Test User");

        when(userService.getUserProfile("1")).thenReturn(profileVO);

        mockMvc.perform(get("/users/profile")
                        .header("User-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("✅ Should upload avatar and update profile successfully")
    void testUploadAvatarAndUpdateProfile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());

        when(fileServiceClient.uploadAvatar(any(MultipartFile.class))).thenReturn(ResponseEntity.ok("http://mockurl/avatar.png"));
        when(userService.getUserProfile(anyString())).thenReturn(new UserProfileVO());
        when(userService.updateUserProfile(anyString(), any(UpdateUserDTO.class))).thenReturn(new UserProfileVO());

        mockMvc.perform(multipart("/users/profile/avatar")
                        .file(file)
                        .header("User-ID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should delete user successfully")
    void testDeleteUser() throws Exception {
        when(userService.deleteUserById("1", "1", "ADMIN")).thenReturn("User deleted successfully");

        mockMvc.perform(delete("/users/1")
                        .header("User-ID", "1")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    @DisplayName("✅ Should request password reset successfully")
    void testRequestPasswordReset() throws Exception {
        mockMvc.perform(post("/users/forgot-password")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(userService).sendResetLink("test@example.com");
    }

    @Test
    @DisplayName("✅ Should reset password successfully")
    void testResetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("newPassword123");

        when(userService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(new UserResponseVO());

        mockMvc.perform(post("/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get users by IDs")
    void testGetUsersByIds() throws Exception {
        when(userService.getUsersByIds(anyList(), isNull())).thenReturn(Collections.singletonList(new UserBriefVO()));

        mockMvc.perform(post("/users/query-by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("1"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get user brief by ID")
    void testGetUserBriefById() throws Exception {
        when(userService.getUserBriefById("1")).thenReturn(new UserBriefVO());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get users by emails")
    void testGetUsersByEmails() throws Exception {
        when(userService.getUsersByEmails(anyList())).thenReturn(Collections.singletonList(new UserBriefVO()));

        mockMvc.perform(post("/users/query-by-emails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("test@example.com"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should list users for admin")
    void testListAllUsersAdmin() throws Exception {
        when(userService.listUsersAdmin(anyString(), anyString(), any(), any(), anyInt(), anyInt(), any(), any())).thenReturn(new PageResponse<>());

        mockMvc.perform(get("/users/admin/list")
                        .header("User-ID", "1")
                        .header("User-Role", "ADMIN")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should redirect to GitHub OAuth page")
    void testRedirectToGithubOauth() throws Exception {
        mockMvc.perform(get("/users/oauth/github")
                        .param("role", "PARTICIPANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("github.com")));
    }

    @Test
    @DisplayName("✅ Should handle GitHub OAuth callback and redirect")
    void testHandleGithubCallback() throws Exception {
        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("github");
        dto.setCode("code");
        dto.setRole("PARTICIPANT");

        when(userService.oauthLoginOrRegister(any(OAuthLoginRequestDTO.class)))
                .thenReturn(new UserResponseVO("userId", "Test User", "test@example.com", "PARTICIPANT", "jwt-token", 3600L));

        mockMvc.perform(get("/users/oauth/callback/github")
                        .param("code", "mock-code")
                        .param("state", "PARTICIPANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("✅ Should redirect to Google OAuth page")
    void testRedirectToGoogleOauth() throws Exception {
        mockMvc.perform(get("/users/oauth/google")
                        .param("role", "PARTICIPANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("accounts.google.com")));
    }

    @Test
    @DisplayName("✅ Should handle Google OAuth callback and redirect")
    void testHandleGoogleCallback() throws Exception {
        OAuthLoginRequestDTO dto = new OAuthLoginRequestDTO();
        dto.setProvider("google");
        dto.setCode("code");
        dto.setRole("PARTICIPANT");

        when(userService.oauthLoginOrRegister(any(OAuthLoginRequestDTO.class)))
                .thenReturn(new UserResponseVO("userId", "Test User", "test@example.com", "PARTICIPANT", "jwt-token", 3600L));

        mockMvc.perform(get("/users/oauth/callback/google")
                        .sessionAttr("oauth_state", "mockState")
                        .param("code", "mockCode")
                        .param("state", "mockState:PARTICIPANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("❌ Should reject Google OAuth callback if state mismatch")
    void testHandleGoogleCallback_StateMismatch() throws Exception {
        mockMvc.perform(get("/users/oauth/callback/google")
                        .sessionAttr("oauth_state", "correctState")
                        .param("code", "mockCode")
                        .param("state", "wrongState:PARTICIPANT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("❌ Should fail logout if Authorization header missing or invalid")
    void testLogoutInvalidHeader() throws Exception {
        mockMvc.perform(post("/users/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));

        mockMvc.perform(post("/users/logout")
                        .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing or invalid Authorization header"));
    }

    @Test
    @DisplayName("✅ Should upload avatar and delete old avatar if exists")
    void testUploadAvatarWithOldAvatarDelete() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "test image".getBytes());

        UserProfileVO profileWithOldAvatar = new UserProfileVO();
        profileWithOldAvatar.setAvatarUrl("http://mock-bucket/mock-folder/old-avatar.png");

        when(fileServiceClient.uploadAvatar(any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok("http://mock-bucket/mock-folder/new-avatar.png"));
        when(userService.getUserProfile(anyString()))
                .thenReturn(profileWithOldAvatar);
        when(userService.updateUserProfile(anyString(), any(UpdateUserDTO.class)))
                .thenReturn(new UserProfileVO());

        mockMvc.perform(multipart("/users/profile/avatar")
                        .file(file)
                        .header("User-ID", "1"))
                .andExpect(status().isOk());

        verify(fileServiceClient, times(1)).deleteFile("mock-folder", "old-avatar.png");

    }

}
