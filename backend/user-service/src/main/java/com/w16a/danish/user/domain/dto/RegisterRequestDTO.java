package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description User Registration Request
 */
@Data
@Schema(name = "RegisterRequestDTO", description = "User Registration Request DTO")
public class RegisterRequestDTO {

    @Schema(description = "User's full name", example = "John Doe")
    @NotBlank(message = "User name cannot be blank")
    private String name;

    @Schema(description = "User email address", example = "johndoe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User password", example = "StrongP@ssw0rd")
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Schema(description = "User role", example = "ADMIN")
    @NotBlank(message = "Role cannot be blank")
    private String role;

}
