package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * Submission Statistics Data Transfer Object
 *
 * @author Eddy ZHANG
 * @date 2025/04/19
 */
@Data
@Schema(description = "Submission Statistics Data")
public class SubmissionStatisticsVO {

    @Schema(description = "Total number of submissions")
    private Integer totalSubmissions;

    @Schema(description = "Number of approved submissions")
    private Integer approvedSubmissions;

    @Schema(description = "Number of pending submissions")
    private Integer pendingSubmissions;

    @Schema(description = "Number of rejected submissions")
    private Integer rejectedSubmissions;
}
