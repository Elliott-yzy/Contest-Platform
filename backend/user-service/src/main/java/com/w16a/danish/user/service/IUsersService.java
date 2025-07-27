package com.w16a.danish.user.service;

import com.w16a.danish.user.domain.dto.*;
import com.w16a.danish.user.domain.po.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.user.domain.vo.*;

import java.util.List;


/**
 * User service interface for managing user operations.
 * Provides functionalities such as registration, authentication,
 * profile management, OAuth login, password reset, and user administration.
 *
 * @author Eddy ZHANG
 * @date 2025/03/16
 */
public interface IUsersService extends IService<Users> {

    /**
     * Registers a new user.
     *
     * @param registerDTO User registration details.
     * @return Registered user information.
     */
    UserResponseVO register(RegisterRequestDTO registerDTO);

    /**
     * Authenticates a user and generates a token.
     *
     * @param loginDTO User login credentials.
     * @return Authenticated user information with token.
     */
    UserResponseVO login(LoginRequestDTO loginDTO);

    /**
     * Deletes a user by ID with permission verification.
     *
     * @param userId ID of the user to be deleted.
     * @param currentUserId ID of the current logged-in user.
     * @param currentUserRole Role of the current logged-in user.
     * @return Operation status message.
     */
    String deleteUserById(String userId, String currentUserId, String currentUserRole);

    /**
     * Retrieves the full profile of a user.
     *
     * @param userId ID of the user.
     * @return User profile details.
     */
    UserProfileVO getUserProfile(String userId);

    /**
     * Handles OAuth login or automatic registration if the user does not exist.
     *
     * @param oAuthLoginRequestDTO OAuth login request details.
     * @return Authenticated or newly registered user information.
     */
    UserResponseVO oauthLoginOrRegister(OAuthLoginRequestDTO oAuthLoginRequestDTO);


    /**
     * Resets the user's password using a secure reset token.
     *
     * @param request Password reset request details.
     * @return Updated user information after password reset.
     */
    UserResponseVO resetPassword(ResetPasswordRequest request);

    /**
     * Sends a password reset link to the specified email address.
     *
     * @param email Target email address.
     */
    void sendResetLink(String email);

    /**
     * Logs out a user by invalidating their authentication token.
     *
     * @param token JWT token of the user to be logged out.
     */
    void logout(String token);

    /**
     * Updates the user's profile information.
     *
     * @param userId ID of the user.
     * @param updateUserDTO User profile update details.
     * @return Updated user profile information.
     */
    UserProfileVO updateUserProfile(String userId, UpdateUserDTO updateUserDTO);

    /**
     * Retrieves a list of brief user information based on user IDs and role.
     *
     * @param userIds List of user IDs.
     * @param role Expected user role for filtering.
     * @return List of brief user information.
     */
    List<UserBriefVO> getUsersByIds(List<String> userIds, String role);

    /**
     * Retrieves brief user information by a single user ID.
     *
     * @param userId ID of the user.
     * @return Brief user information.
     */
    UserBriefVO getUserBriefById(String userId);

    /**
     * Retrieves a list of brief user information based on email addresses.
     *
     * @param emails List of user email addresses.
     * @return List of brief user information.
     */
    List<UserBriefVO> getUsersByEmails(List<String> emails);

    /**
     * Retrieves a paginated list of users for admin management.
     *
     * @param adminId ID of the admin making the request.
     * @param adminRole Role of the admin.
     * @param role Role of users to filter.
     * @param keyword Keyword for search filtering (e.g., name or email).
     * @param page Page number (starting from 1).
     * @param size Number of records per page.
     * @param sortBy Field to sort by.
     * @param order Sorting order ("asc" or "desc").
     * @return Paginated list of user information for admin view.
     */
    PageResponse<AdminUserVO> listUsersAdmin(String adminId, String adminRole, String role, String keyword, int page, int size, String sortBy, String order);

}
