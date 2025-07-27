package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Eddy ZHANG
 * @date 2025/03/25
 * @description OAuth login request DTO
 */
@Data
@Schema(name = "OAuthLoginRequestDTO", description = "OAuth login request DTO")
public class OAuthLoginRequestDTO {
    @Schema(description = "OAuth provider", example = "github")
    private String provider;

    @Schema(description = "OAuth code", example = "abc123")
    private String code;

    @Schema(description = "User selected role", example = "ORGANIZER")
    private String role;
}