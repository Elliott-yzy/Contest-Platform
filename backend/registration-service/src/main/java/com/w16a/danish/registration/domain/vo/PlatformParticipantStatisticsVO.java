package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * This class represents the platform-wide participant statistics for the dashboard overview.
 *
 * @author Eddy ZHANG
 * @date 2025/04/20
 */
@Data
@Schema(name = "PlatformParticipantStatisticsVO", description = "Platform-wide participant statistics for dashboard overview")
public class PlatformParticipantStatisticsVO {

    @Schema(description = "Total number of participants (individual + team members)", example = "1500")
    private Integer totalParticipants;

    @Schema(description = "Number of individual participants", example = "900")
    private Integer individualParticipants;

    @Schema(description = "Number of team participants (team members)", example = "600")
    private Integer teamParticipants;
}
