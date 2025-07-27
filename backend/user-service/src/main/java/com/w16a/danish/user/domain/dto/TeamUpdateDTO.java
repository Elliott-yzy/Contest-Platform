package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for updating team information.
 *
 * @author Eddy
 * @since 2025/04/17
 */
@Data
@Schema(description = "DTO for updating a team's information")
public class TeamUpdateDTO {

    @Schema(description = "New team name", example = "VisionAI Pro")
    @NotBlank(message = "Team name cannot be empty")
    private String name;

    @Schema(description = "Updated team description", example = "An AI team focusing on cutting-edge innovation.")
    private String description;
}
