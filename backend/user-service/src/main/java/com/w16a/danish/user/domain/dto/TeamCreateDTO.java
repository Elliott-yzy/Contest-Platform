package com.w16a.danish.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for creating a new team.
 *
 * @author Eddy
 * @since 2025/04/16
 */
@Data
@Schema(description = "DTO for creating a new team")
public class TeamCreateDTO {

    @Schema(description = "Team name", example = "AI Masters")
    @NotBlank(message = "Team name must not be blank")
    private String name;

    @Schema(description = "Team description", example = "We are a group of AI enthusiasts participating in competitions together.")
    private String description;
}
