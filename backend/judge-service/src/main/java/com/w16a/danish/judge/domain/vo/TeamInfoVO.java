package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 * View Object for basic information about a registered team.
 *
 * @author Eddy ZHANG
 * @date 2025/04/17
 */
@Data
@Schema(name = "TeamInfoVO", description = "Basic information about a registered team")
public class TeamInfoVO {

    @Schema(description = "Team ID", example = "team-uuid-1234")
    private String teamId;

    @Schema(description = "Team name", example = "AI Innovators")
    private String teamName;

    @Schema(description = "Team description", example = "Passionate about solving real-world problems using AI.")
    private String description;

    @Schema(description = "Team creation time", example = "2025-04-01T10:30:00")
    private LocalDateTime createdAt;

}
