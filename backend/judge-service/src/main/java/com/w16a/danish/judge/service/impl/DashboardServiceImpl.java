package com.w16a.danish.judge.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.w16a.danish.judge.domain.enums.ParticipationType;
import com.w16a.danish.judge.domain.vo.CompetitionDashboardVO;
import com.w16a.danish.judge.domain.vo.PlatformDashboardVO;
import com.w16a.danish.judge.exception.BusinessException;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.InteractionServiceClient;
import com.w16a.danish.judge.feign.SubmissionServiceClient;
import com.w16a.danish.judge.feign.UserServiceClient;
import com.w16a.danish.judge.service.ICompetitionJudgesService;
import com.w16a.danish.judge.service.IDashboardService;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the Dashboard Service, retrieving statistics via Feign clients.
 * Fully decoupled from direct database access.
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final CompetitionServiceClient competitionServiceClient;
    private final SubmissionServiceClient registrationServiceClient;
    private final InteractionServiceClient interactionServiceClient;
    private final ICompetitionJudgesService competitionJudgesService;
    private final ISubmissionRecordsService submissionRecordsService;
    private final UserServiceClient userServiceClient;

    @Override
    public CompetitionDashboardVO getCompetitionStatistics(String competitionId, String userId) {
        var competitionResp = competitionServiceClient.getCompetitionById(competitionId);
        var competition = competitionResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        CompetitionDashboardVO dashboard = new CompetitionDashboardVO();
        dashboard.setCompetitionName(competition.getName());
        dashboard.setCompetitionStatus(competition.getStatus().getValue());
        dashboard.setParticipationType(competition.getParticipationType().name());

        var registrationStatsResp = registrationServiceClient.getRegistrationStatistics(competitionId);
        var registrationStats = registrationStatsResp.getBody();
        if (registrationStats != null) {
            dashboard.setIndividualParticipantCount(registrationStats.getIndividualParticipantCount());
            dashboard.setTeamParticipantCount(registrationStats.getTeamParticipantCount());
        }

        var submissionStatsResp = registrationServiceClient.getSubmissionStatistics(competitionId);
        var submissionStats = submissionStatsResp.getBody();
        if (submissionStats != null) {
            dashboard.setSubmissionCount(submissionStats.getTotalSubmissions());
            dashboard.setApprovedSubmissionCount(submissionStats.getApprovedSubmissions());
            dashboard.setPendingSubmissionCount(submissionStats.getPendingSubmissions());
        }

        var interactionStatsResp = interactionServiceClient.getInteractionStatistics(competitionId);
        var interactionStats = interactionStatsResp.getBody();
        if (interactionStats != null) {
            dashboard.setVoteCount(interactionStats.getVoteCount() != null ? Math.toIntExact(interactionStats.getVoteCount()) : 0);
            dashboard.setCommentCount(interactionStats.getCommentCount() != null ? Math.toIntExact(interactionStats.getCommentCount()) : 0);
        }

        int judgeCount = competitionJudgesService.countJudgesByCompetitionId(competitionId);
        dashboard.setJudgeCount(judgeCount);

        var scoreStats = submissionRecordsService.getSubmissionScoreStatistics(competitionId);
        if (scoreStats != null) {
            dashboard.setAverageScore(scoreStats.getAverageScore());
            dashboard.setHighestScore(scoreStats.getHighestScore());
            dashboard.setLowestScore(scoreStats.getLowestScore());
        }

        Optional.ofNullable(registrationServiceClient.getParticipantTrend(competitionId))
                .map(ResponseEntity::getBody)
                .ifPresent(participantTrendMap -> {
                    Map<String, Integer> individualTrend = Optional.ofNullable(participantTrendMap.get("individual")).orElseGet(Map::of);
                    Map<String, Integer> teamTrend = Optional.ofNullable(participantTrendMap.get("team")).orElseGet(Map::of);

                    dashboard.setIndividualParticipantTrend(individualTrend);
                    dashboard.setTeamParticipantTrend(teamTrend);
                });

        Optional.ofNullable(registrationServiceClient.getSubmissionTrend(competitionId))
                .map(ResponseEntity::getBody)
                .ifPresent(dashboard::setSubmissionTrend);

        if (userId != null) {
            boolean hasSubmitted = false;

            ParticipationType participationType = competition.getParticipationType();
            if (participationType == ParticipationType.INDIVIDUAL) {
                var mySubmission = submissionRecordsService.getMySubmissionBasic(competitionId, userId);
                if (mySubmission != null) {
                    hasSubmitted = true;
                    dashboard.setMyTotalScore(mySubmission.getTotalScore());
                    dashboard.setMyReviewStatus(mySubmission.getReviewStatus());
                }
            } else if (participationType == ParticipationType.TEAM) {
                var teamIdsResp = userServiceClient.getJoinedTeamIdsByUser(userId);
                List<String> teamIds = teamIdsResp.getBody();

                if (CollUtil.isNotEmpty(teamIds)) {
                    for (String teamId : teamIds) {
                        var teamSubmission = submissionRecordsService.getTeamSubmissionBasic(competitionId, teamId);
                        if (teamSubmission != null) {
                            hasSubmitted = true;
                            dashboard.setMyTotalScore(teamSubmission.getTotalScore());
                            dashboard.setMyReviewStatus(teamSubmission.getReviewStatus());
                            break;
                        }
                    }
                }
            }
            dashboard.setHasSubmitted(hasSubmitted);
        } else {
            dashboard.setHasSubmitted(false);
        }

        return dashboard;
    }

    @Override
    public PlatformDashboardVO getPlatformDashboard() {
        PlatformDashboardVO dashboard = new PlatformDashboardVO();

        var competitions = Optional.ofNullable(competitionServiceClient.listAllCompetitions().getBody())
                .orElse(Collections.emptyList());

        if (competitions.isEmpty()) {
            return dashboard;
        }

        int totalCompetitions = competitions.size();
        int individualCompetitions = 0;
        int teamCompetitions = 0;
        int activeCompetitions = 0;
        int finishedCompetitions = 0;

        for (var competition : competitions) {
            if (competition.getParticipationType() == ParticipationType.INDIVIDUAL) {
                individualCompetitions++;
            } else if (competition.getParticipationType() == ParticipationType.TEAM) {
                teamCompetitions++;
            }

            if (competition.getStatus() != null) {
                switch (competition.getStatus()) {
                    case ONGOING -> activeCompetitions++;
                    case COMPLETED -> finishedCompetitions++;
                    default -> {
                    }
                }
            }
        }

        dashboard.setTotalCompetitions(totalCompetitions);
        dashboard.setIndividualCompetitions(individualCompetitions);
        dashboard.setTeamCompetitions(teamCompetitions);
        dashboard.setActiveCompetitions(activeCompetitions);
        dashboard.setFinishedCompetitions(finishedCompetitions);

        var participantStatsResp = registrationServiceClient.getPlatformParticipantStatistics();
        var participantStats = participantStatsResp.getBody();
        if (participantStats != null) {
            dashboard.setTotalParticipants(participantStats.getTotalParticipants());
            dashboard.setIndividualParticipants(participantStats.getIndividualParticipants());
            dashboard.setTeamParticipants(participantStats.getTeamParticipants());
        }

        var submissionStatsResp = registrationServiceClient.getPlatformSubmissionStatistics();
        var submissionStats = submissionStatsResp.getBody();
        if (submissionStats != null) {
            dashboard.setTotalSubmissions(submissionStats.getTotalSubmissions());
            dashboard.setApprovedSubmissions(submissionStats.getApprovedSubmissions());
            dashboard.setIndividualSubmissions(submissionStats.getIndividualSubmissions());
            dashboard.setTeamSubmissions(submissionStats.getTeamSubmissions());
        }

        var interactionStatsResp = interactionServiceClient.getPlatformInteractionStatistics();
        var interactionStats = interactionStatsResp.getBody();
        if (interactionStats != null) {
            dashboard.setTotalVotes(Optional.ofNullable(interactionStats.getVoteCount()).map(Math::toIntExact).orElse(0));
            dashboard.setTotalComments(Optional.ofNullable(interactionStats.getCommentCount()).map(Math::toIntExact).orElse(0));
        }

        var participantTrendResp = registrationServiceClient.getPlatformParticipantTrend();
        var participantTrend = participantTrendResp.getBody();
        if (participantTrend != null) {
            dashboard.setParticipantTrend(participantTrend);
        }

        var submissionTrendResp = registrationServiceClient.getPlatformSubmissionTrend();
        var submissionTrend = submissionTrendResp.getBody();
        if (submissionTrend != null) {
            dashboard.setSubmissionTrend(submissionTrend);
        }

        return dashboard;
    }


}
