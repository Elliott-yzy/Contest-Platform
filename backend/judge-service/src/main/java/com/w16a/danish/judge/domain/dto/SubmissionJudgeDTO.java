package com.w16a.danish.judge.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DTO for judge submitting their scoring for a submission.
 * Encapsulates overall comments + individual criterion scores.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@Schema(description = "Data transfer object for a judge to submit their evaluation on a submission.")
public class SubmissionJudgeDTO {

    @Schema(description = "Competition ID", example = "comp-123e4567-e89b-12d3-a456-426614174000", required = true)
    private String competitionId;

    @Schema(description = "Submission ID being judged", example = "sub-123e4567-e89b-12d3-a456-426614174000", required = true)
    private String submissionId;

    @Schema(description = "General comments provided by the judge", example = "Excellent innovation, but documentation could be improved.", required = false)
    private String judgeComments;

    @Schema(description = "List of scores for each evaluation criterion", required = true)
    private List<CriterionScoreDTO> scores;
}
