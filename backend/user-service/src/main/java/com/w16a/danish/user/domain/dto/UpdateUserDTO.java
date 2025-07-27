package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description Update User Information Request
 */
@Data
@Schema(name = "UpdateUserDTO", description = "Request payload for updating user profile")
public class UpdateUserDTO {

    @Schema(description = "New email address (if changing)", example = "newemail@example.com", nullable = true)
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Updated user name", example = "John Doe", nullable = true)
    private String name;

    @Schema(description = "New password (if changing)", example = "NewStr0ngPass!", nullable = true)
    private String password;

    @Schema(description = "Updated user description or bio", example = "Senior Developer at XYZ Corp.", nullable = true)
    private String description;

    @Schema(description = "New avatar URL", example = "https://cdn.example.com/avatar.jpg", nullable = true)
    private String avatarUrl;
}

