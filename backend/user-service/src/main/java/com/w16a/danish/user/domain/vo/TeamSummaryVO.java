package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * View Object for summarizing team information (e.g. in a list view).
 * Typically used for "My Teams", "All Teams", or "Search Teams" API responses.
 *
 * @author Eddy
 * @since 2025/04/16
 */
@Data
@Schema(description = "Summary view of a team")
public class TeamSummaryVO {

    @Schema(description = "Team ID", example = "2f65debe-8b34-429c-9b0e-d18f0643cbe5")
    private String id;

    @Schema(description = "Team name", example = "AI Masters")
    private String name;

    @Schema(description = "Team description", example = "A passionate team focusing on machine learning competitions.")
    private String description;

    @Schema(description = "Team leader name", example = "Eddy Zhang")
    private String leaderName;

    @Schema(description = "Number of members in the team", example = "4")
    private Integer memberCount;

    @Schema(description = "Creation timestamp", example = "2025-04-16T09:32:00")
    private LocalDateTime createdAt;
}
