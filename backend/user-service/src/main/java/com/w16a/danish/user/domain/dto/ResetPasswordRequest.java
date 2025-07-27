package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Reset password request DTO
 */
@Data
@Schema(description = "Reset password request DTO")
public class ResetPasswordRequest {
    @Schema(description = "Reset token from email link")
    private String token;

    @Schema(description = "New secure password")
    private String newPassword;
}
