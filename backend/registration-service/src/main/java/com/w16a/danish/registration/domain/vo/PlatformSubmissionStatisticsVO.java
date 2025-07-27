package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 *
 * Platform-wide submission statistics (individual and team)
 *
 * @author Eddy ZHANG
 * @date 2025/04/20
 */
@Data
@Schema(name = "PlatformSubmissionStatisticsVO", description = "Platform-wide submission statistics (individual and team)")
public class PlatformSubmissionStatisticsVO {

    @Schema(description = "Total number of submissions across the platform")
    private Integer totalSubmissions;

    @Schema(description = "Total number of approved submissions")
    private Integer approvedSubmissions;

    @Schema(description = "Total number of individual (personal) submissions")
    private Integer individualSubmissions;

    @Schema(description = "Total number of team submissions")
    private Integer teamSubmissions;
}
