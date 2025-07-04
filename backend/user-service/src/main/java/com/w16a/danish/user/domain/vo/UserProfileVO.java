package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Eddy ZHANG
 * @date 2025/03/17
 * @description User Profile Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserProfileVO", description = "User Profile Information")
public class UserProfileVO {

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "User email address", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Short description or bio of the user", example = "Software Engineer at ABC Corp.", nullable = true)
    private String description;

    @Schema(description = "URL to the user's avatar image", example = "https://example.com/avatar.jpg", nullable = true)
    private String avatarUrl;
}
