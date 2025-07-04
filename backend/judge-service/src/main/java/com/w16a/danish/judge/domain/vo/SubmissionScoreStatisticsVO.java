package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


/**
 *
 * Submission Score Statistics Value Object
 *
 * @author Eddy ZHANG
 * @date 2025/04/19
 */
@Data
@Schema(description = "Submission Score Statistics")
public class SubmissionScoreStatisticsVO {

    @Schema(description = "Average score")
    private BigDecimal averageScore;

    @Schema(description = "Highest score")
    private BigDecimal highestScore;

    @Schema(description = "Lowest score")
    private BigDecimal lowestScore;
}
