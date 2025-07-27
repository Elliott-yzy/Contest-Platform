package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View Object for displaying awarded submission information.
 * Used in public winner listing.
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Data
@Schema(name = "WinnerInfoVO", description = "Information of a winning submission")
public class WinnerInfoVO {

    @Schema(description = "Submission ID", example = "subm-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Title of the submission", example = "AI-Based Smart Home System")
    private String title;

    @Schema(description = "List of awards won by this submission", example = "[\"Champion\", \"Best in Innovation\"]")
    private List<String> awards;

    @Schema(description = "Total score given to the submission", example = "92.5")
    private BigDecimal totalScore;

    @Schema(description = "Name of the user or team that submitted", example = "Team Alpha" /* or "John Doe"*/)
    private String submitterName;

    @Schema(description = "Whether the submission was made by a team", example = "true")
    private Boolean isTeamSubmission;

    @Schema(description = "Submission creation time", example = "2025-04-10T14:30:00")
    private LocalDateTime submittedAt;
}
