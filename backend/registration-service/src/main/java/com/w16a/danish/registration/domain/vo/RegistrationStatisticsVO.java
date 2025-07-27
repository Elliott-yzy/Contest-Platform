package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * View Object for competition registration statistics.
 * Shows counts of participants and teams.
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Data
@Schema(description = "Competition Registration Statistics")
public class RegistrationStatisticsVO {

    @Schema(description = "Competition ID")
    private String competitionId;

    @Schema(description = "Total number of individual participants")
    private Integer individualParticipantCount;

    @Schema(description = "Total number of registered teams")
    private Integer teamParticipantCount;

    @Schema(description = "Total number of registrations (individuals + teams)")
    private Integer totalRegistrations;
}
