package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Eddy
 * @date 2025/04/03
 * @description View Object for user's competition participation record
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CompetitionParticipationVO", description = "User's participation information in a competition")
public class CompetitionParticipationVO {

    @Schema(description = "Competition ID", example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f")
    private String competitionId;

    @Schema(description = "Competition name", example = "AI Coding Challenge")
    private String competitionName;

    @Schema(description = "Competition category", example = "Web Development")
    private String category;

    @Schema(description = "Competition status", example = "ONGOING")
    private String status;

    @Schema(description = "Start time", example = "2025-05-01T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "End time", example = "2025-06-01T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Whether the competition is public", example = "true")
    private Boolean isPublic;

    @Schema(description = "User's participation registration time", example = "2025-04-03T14:22:00")
    private LocalDateTime joinedAt;

    @Schema(description = "Has the participant submitted work", example = "true")
    private Boolean hasSubmitted;

    @Schema(description = "Final score (if evaluated)", example = "85.50")
    private BigDecimal totalScore;

}
