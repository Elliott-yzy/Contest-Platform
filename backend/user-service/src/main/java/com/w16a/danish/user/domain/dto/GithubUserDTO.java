package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Github User DTO
 */
@Data
@Schema(name = "GithubUserDTO", description = "Github user DTO")
public class GithubUserDTO {
    @Schema(description = "Github user ID")
    private String login;

    @Schema(description = "Github user email")
    private String email;

}

