package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View object for displaying a judge's scoring details on a submission.
 * Includes total score, judge comments, and detailed criterion scores.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@Schema(name = "SubmissionJudgeVO", description = "Detailed judging information for a submission")
public class SubmissionJudgeVO {

    @Schema(description = "Submission ID", example = "subm-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Competition ID", example = "comp-987e6543-e21b-34d3-bb3c-8950e4d1cf3f")
    private String competitionId;

    @Schema(description = "Judge's user ID", example = "judge-567e8910-a12b-45d8-bc2f-9950e4d1ff3a")
    private String judgeId;

    @Schema(description = "Judge's overall comments for the submission", example = "Very innovative and well-presented.")
    private String judgeComments;

    @Schema(description = "Total score assigned by the judge", example = "85.50")
    private BigDecimal totalScore;

    @Schema(description = "Submission judging created timestamp", example = "2025-06-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Submission judging last updated timestamp", example = "2025-06-02T15:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of individual criterion scores")
    private List<CriterionScoreVO> scores;

    /**
     * Inner class representing score details for each criterion.
     */
    @Data
    @Schema(name = "CriterionScoreVO", description = "Score assigned for each criterion")
    public static class CriterionScoreVO {

        @Schema(description = "Scoring criterion name", example = "Innovation")
        private String criterion;

        @Schema(description = "Score assigned to this criterion", example = "8.5")
        private BigDecimal score;

        @Schema(description = "Weight of this criterion in total score", example = "0.4")
        private BigDecimal weight;
    }
}
