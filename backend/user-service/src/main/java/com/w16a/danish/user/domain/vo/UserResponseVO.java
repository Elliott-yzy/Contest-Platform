package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Eddy ZHANG
 * @date 2025/03/17
 * @description User Registration or Login Response Data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserResponseVO", description = "Response data after user registration or login")
public class UserResponseVO {

    @Schema(description = "Unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "User email address", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "ADMIN")
    private String role;

    @Schema(description = "JWT authentication token for API access", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token expiration time in seconds", example = "3600")
    private long expiresIn;
}
