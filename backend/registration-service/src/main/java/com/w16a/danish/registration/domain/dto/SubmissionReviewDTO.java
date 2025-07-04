package com.w16a.danish.registration.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * This class represents the data transfer object for reviewing a submission.
 *
 * @author Eddy ZHANG
 * @date 2025/04/06
 */
@Data
@Schema(name = "SubmissionReviewDTO", description = "Request body for reviewing a submission")
public class SubmissionReviewDTO {

    @Schema(description = "Submission ID to review", required = true)
    private String submissionId;

    @Schema(description = "Review decision: APPROVED / REJECTED", required = true, example = "APPROVED")
    private String reviewStatus;

    @Schema(description = "Review comment or feedback", example = "Great submission, well structured.")
    private String reviewComments;

}
