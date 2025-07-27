package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description User Login Request DTO
 */
@Data
@Schema(name = "LoginRequestDTO", description = "User Login Request DTO")
public class LoginRequestDTO {

    @Schema(description = "User email address", example = "johndoe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User password", example = "StrongP@ssw0rd")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Schema(description = "User role (PARTICIPANT or ORGANIZER)", example = "PARTICIPANT")
    @NotBlank(message = "Role cannot be blank")
    private String role;
}
