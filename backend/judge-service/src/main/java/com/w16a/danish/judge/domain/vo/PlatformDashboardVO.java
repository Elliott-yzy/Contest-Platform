package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * This class represents the platform-wide detailed dashboard overview for the competition platform.
 * Includes statistics on competitions, participants, submissions, interactions, and trends.
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Data
@Schema(name = "PlatformDashboardVO", description = "Platform-wide detailed dashboard overview for the competition platform")
public class PlatformDashboardVO {

    @Schema(description = "Total number of competitions on the platform", example = "120")
    private Integer totalCompetitions;

    @Schema(description = "Number of individual competitions", example = "70")
    private Integer individualCompetitions;

    @Schema(description = "Number of team competitions", example = "50")
    private Integer teamCompetitions;

    @Schema(description = "Number of competitions currently ongoing", example = "45")
    private Integer activeCompetitions;

    @Schema(description = "Number of competitions that have finished", example = "65")
    private Integer finishedCompetitions;

    @Schema(description = "Total number of participants (individual + team members)", example = "1500")
    private Integer totalParticipants;

    @Schema(description = "Number of individual participants", example = "900")
    private Integer individualParticipants;

    @Schema(description = "Number of team participants (team members)", example = "600")
    private Integer teamParticipants;

    @Schema(description = "Total number of submissions uploaded", example = "860")
    private Integer totalSubmissions;

    @Schema(description = "Number of submissions from individual competitions", example = "500")
    private Integer individualSubmissions;

    @Schema(description = "Number of submissions from team competitions", example = "360")
    private Integer teamSubmissions;

    @Schema(description = "Number of approved submissions", example = "620")
    private Integer approvedSubmissions;

    @Schema(description = "Total number of votes cast across all competitions", example = "3200")
    private Integer totalVotes;

    @Schema(description = "Total number of comments posted across all competitions", example = "1450")
    private Integer totalComments;

    @Schema(description = "Daily trend of participant registrations (individual and team)", example = "{\"individual\":{\"2025-04-01\":15}, \"team\":{\"2025-04-01\":8}}")
    private Map<String, Map<String, Integer>> participantTrend;

    @Schema(description = "Daily trend of submission uploads (date → new submissions)", example = "{\"2025-04-01\":8, \"2025-04-02\":20}")
    private Map<String, Integer> submissionTrend;

}
