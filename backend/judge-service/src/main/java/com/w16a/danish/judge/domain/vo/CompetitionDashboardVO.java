package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Competition Dashboard Data Transfer Object
 * Provides an overview of competition statistics.
 * Extended for participant personal info and growth trends.
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Data
@Schema(description = "Competition Dashboard Data")
public class CompetitionDashboardVO {

    @Schema(description = "Competition name")
    private String competitionName;

    @Schema(description = "Competition status (e.g., DRAFT, ONGOING, ENDED)")
    private String competitionStatus;

    @Schema(description = "Participation type: INDIVIDUAL or TEAM")
    private String participationType;

    @Schema(description = "Number of individual participants")
    private Integer individualParticipantCount;

    @Schema(description = "Number of teams participating")
    private Integer teamParticipantCount;

    @Schema(description = "Total number of submissions (works)")
    private Integer submissionCount;

    @Schema(description = "Number of approved submissions")
    private Integer approvedSubmissionCount;

    @Schema(description = "Number of pending submissions")
    private Integer pendingSubmissionCount;

    @Schema(description = "Total number of votes")
    private Integer voteCount;

    @Schema(description = "Total number of comments")
    private Integer commentCount;

    @Schema(description = "Total number of judges assigned")
    private Integer judgeCount;

    @Schema(description = "Average score across all scored submissions")
    private BigDecimal averageScore;

    @Schema(description = "Highest score awarded")
    private BigDecimal highestScore;

    @Schema(description = "Lowest score awarded")
    private BigDecimal lowestScore;

    // ====== New fields for participant personal submission info ======

    @Schema(description = "If logged in: whether the user has submitted a work")
    private Boolean hasSubmitted;

    @Schema(description = "If logged in: user's submission total score (if available)")
    private BigDecimal myTotalScore;

    @Schema(description = "If logged in: user's submission review status (e.g., PENDING, APPROVED, REJECTED)")
    private String myReviewStatus;

    // ====== New fields for participant and submission trends ======

    @Schema(description = "Individual participant registration trend (date -> number)")
    private Map<String, Integer> individualParticipantTrend;

    @Schema(description = "Team participant registration trend (date -> number)")
    private Map<String, Integer> teamParticipantTrend;

    @Schema(description = "Submission upload trend (date -> number)")
    private Map<String, Integer> submissionTrend;

}
