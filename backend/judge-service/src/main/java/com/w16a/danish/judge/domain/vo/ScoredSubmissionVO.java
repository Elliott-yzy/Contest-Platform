package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents a submission with total score and detailed criterion scores
 * for organizer/admin to view and manage awards.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@Schema(name = "ScoredSubmissionVO", description = "Submission with total and detailed scores for awarding")
public class ScoredSubmissionVO {

    @Schema(description = "Submission ID", example = "subm-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Title of the submission", example = "AI Smart Farming System")
    private String title;

    @Schema(description = "Total score aggregated from all judges", example = "87.50")
    private BigDecimal totalScore;

    @Schema(description = "Detailed scores for each criterion (criterion name -> average score)",
            example = "{\"Innovation\": 9.0, \"Technical Implementation\": 8.5, \"Usability\": 8.8}")
    private Map<String, BigDecimal> criterionScores;

    @Schema(description = "Whether this submission has been marked as a winner", example = "false")
    private Boolean isWinner;

}
