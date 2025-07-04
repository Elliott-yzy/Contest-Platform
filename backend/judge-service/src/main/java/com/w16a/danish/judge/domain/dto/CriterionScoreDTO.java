package com.w16a.danish.judge.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for representing an individual criterion's score given by a judge.
 * One Submission can have multiple such criterion scores.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@Schema(description = "Individual scoring item under a judge's evaluation.")
public class CriterionScoreDTO {

    @Schema(description = "Criterion name", example = "Creativity", required = true)
    private String criterion;

    @Schema(description = "Score assigned for this criterion", example = "8.5", required = true)
    private BigDecimal score;

    @Schema(description = "Weight of this criterion", example = "0.4", required = true)
    private BigDecimal weight;
}
