package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Admin view of user list (for management purposes).
 * Includes role information and basic user details.
 *
 * @author Eddy
 * @date 2025/04/21
 */
@Data
@Builder
@Schema(name = "AdminUserVO", description = "Admin view of user list, including user role and basic info")
public class AdminUserVO {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "User name", example = "Eddy Zhang")
    private String name;

    @Schema(description = "User email address", example = "eddy@example.com")
    private String email;

    @Schema(description = "User role (PARTICIPANT, ORGANIZER, JUDGE, ADMIN)", example = "PARTICIPANT")
    private String role;

    @Schema(description = "Avatar URL", example = "https://cdn.example.com/avatars/avatar123.jpg")
    private String avatarUrl;

    @Schema(description = "User description/bio", example = "Software engineer, AI enthusiast.")
    private String description;

    @Schema(description = "Account creation time", example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt;
}
