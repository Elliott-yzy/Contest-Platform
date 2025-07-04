package com.w16a.danish.judge.service.impl;

import com.w16a.danish.judge.domain.enums.CompetitionStatus;
import com.w16a.danish.judge.domain.enums.ParticipationType;
import com.w16a.danish.judge.domain.vo.*;
import com.w16a.danish.judge.exception.BusinessException;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.InteractionServiceClient;
import com.w16a.danish.judge.feign.SubmissionServiceClient;
import com.w16a.danish.judge.feign.UserServiceClient;
import com.w16a.danish.judge.service.ICompetitionJudgesService;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardServiceImpl}.
 */
class DashboardServiceImplTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Mock private CompetitionServiceClient competitionServiceClient;
    @Mock private SubmissionServiceClient registrationServiceClient;
    @Mock private InteractionServiceClient interactionServiceClient;
    @Mock private ICompetitionJudgesService competitionJudgesService;
    @Mock private ISubmissionRecordsService submissionRecordsService;
    @Mock private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Should return competition statistics for an individual participant successfully")
    void testGetCompetitionStatistics_ForIndividualParticipant() {
        // Arrange - Mock competition basic info
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setName("BigBrain Contest");
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setParticipationType(ParticipationType.INDIVIDUAL);
        when(competitionServiceClient.getCompetitionById(any()))
                .thenReturn(ResponseEntity.ok(competition));

        // Arrange - Mock registration statistics
        RegistrationStatisticsVO registrationStats = new RegistrationStatisticsVO();
        registrationStats.setIndividualParticipantCount(100);
        registrationStats.setTeamParticipantCount(20);
        registrationStats.setTotalRegistrations(120);
        when(registrationServiceClient.getRegistrationStatistics(any()))
                .thenReturn(ResponseEntity.ok(registrationStats));

        // Arrange - Mock submission statistics
        SubmissionStatisticsVO submissionStats = new SubmissionStatisticsVO();
        submissionStats.setTotalSubmissions(50);
        submissionStats.setApprovedSubmissions(30);
        submissionStats.setPendingSubmissions(20);
        when(registrationServiceClient.getSubmissionStatistics(any()))
                .thenReturn(ResponseEntity.ok(submissionStats));

        // Arrange - Mock interaction statistics
        InteractionStatisticsVO interactionStats = new InteractionStatisticsVO();
        interactionStats.setVoteCount(200L);
        interactionStats.setCommentCount(50L);
        when(interactionServiceClient.getInteractionStatistics(any()))
                .thenReturn(ResponseEntity.ok(interactionStats));

        // Arrange - Mock judge count
        when(competitionJudgesService.countJudgesByCompetitionId(any()))
                .thenReturn(5);

        // Arrange - Mock score statistics
        SubmissionScoreStatisticsVO scoreStats = new SubmissionScoreStatisticsVO();
        scoreStats.setAverageScore(BigDecimal.valueOf(80));
        scoreStats.setHighestScore(BigDecimal.valueOf(100));
        scoreStats.setLowestScore(BigDecimal.valueOf(60));
        when(submissionRecordsService.getSubmissionScoreStatistics(any()))
                .thenReturn(scoreStats);

        // Arrange - Mock user's own submission
        SubmissionInfoVO mySubmission = new SubmissionInfoVO();
        mySubmission.setTotalScore(BigDecimal.valueOf(88));
        mySubmission.setReviewStatus("APPROVED");
        when(submissionRecordsService.getMySubmissionBasic(any(), any()))
                .thenReturn(mySubmission);

        // Arrange - Mock trends
        when(registrationServiceClient.getParticipantTrend(any()))
                .thenReturn(ResponseEntity.ok(Map.of("individual", Map.of("2025-01", 10))));
        when(registrationServiceClient.getSubmissionTrend(any()))
                .thenReturn(ResponseEntity.ok(Map.of("2025-01", 5)));

        // Act
        CompetitionDashboardVO dashboard = dashboardService.getCompetitionStatistics("competitionId", "userId");

        // Assert
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getCompetitionName()).isEqualTo("BigBrain Contest");
        assertThat(dashboard.getParticipationType()).isEqualTo("INDIVIDUAL");
        assertThat(dashboard.getIndividualParticipantCount()).isEqualTo(100);
        assertThat(dashboard.getSubmissionCount()).isEqualTo(50);
        assertThat(dashboard.getVoteCount()).isEqualTo(200);
        assertThat(dashboard.getJudgeCount()).isEqualTo(5);
        assertThat(dashboard.getAverageScore()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(dashboard.getHasSubmitted()).isTrue();
        assertThat(dashboard.getMyTotalScore()).isEqualTo(BigDecimal.valueOf(88));
    }

    @Test
    @DisplayName("✅ Should return competition statistics for a team participant successfully")
    void testGetCompetitionStatistics_ForTeamParticipant() {
        // Arrange
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setParticipationType(ParticipationType.TEAM);
        competition.setStatus(CompetitionStatus.ONGOING);

        when(competitionServiceClient.getCompetitionById(any()))
                .thenReturn(ResponseEntity.ok(competition));

        when(userServiceClient.getJoinedTeamIdsByUser(any()))
                .thenReturn(ResponseEntity.ok(List.of("team-1")));

        SubmissionInfoVO teamSubmission = new SubmissionInfoVO();
        teamSubmission.setTotalScore(BigDecimal.valueOf(92));
        teamSubmission.setReviewStatus("APPROVED");

        when(submissionRecordsService.getTeamSubmissionBasic(any(), any()))
                .thenReturn(teamSubmission);

        // 🛠 Mock registration stats
        RegistrationStatisticsVO registrationStats = new RegistrationStatisticsVO();
        registrationStats.setIndividualParticipantCount(10);
        registrationStats.setTeamParticipantCount(5);
        registrationStats.setTotalRegistrations(15);

        when(registrationServiceClient.getRegistrationStatistics(any()))
                .thenReturn(ResponseEntity.ok(registrationStats));

        // 🛠 Mock submission stats
        SubmissionStatisticsVO submissionStats = new SubmissionStatisticsVO();
        submissionStats.setTotalSubmissions(20);
        submissionStats.setApprovedSubmissions(15);
        submissionStats.setPendingSubmissions(5);

        when(registrationServiceClient.getSubmissionStatistics(any()))
                .thenReturn(ResponseEntity.ok(submissionStats));

        // 🛠 Mock interaction stats
        InteractionStatisticsVO interactionStats = new InteractionStatisticsVO();
        interactionStats.setVoteCount(100L);
        interactionStats.setCommentCount(30L);

        when(interactionServiceClient.getInteractionStatistics(any()))
                .thenReturn(ResponseEntity.ok(interactionStats));

        // Act
        CompetitionDashboardVO dashboard = dashboardService.getCompetitionStatistics("competitionId", "userId");

        // Assert
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getHasSubmitted()).isTrue();
        assertThat(dashboard.getMyTotalScore()).isEqualTo(BigDecimal.valueOf(92));
        assertThat(dashboard.getCompetitionStatus()).isEqualTo(CompetitionStatus.ONGOING.getValue());
    }

    @Test
    @DisplayName("❌ Should throw BusinessException if competition not found")
    void testGetCompetitionStatistics_NotFound() {
        when(competitionServiceClient.getCompetitionById(any()))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> dashboardService.getCompetitionStatistics("competitionId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Competition not found");
    }

    @Test
    @DisplayName("✅ Should return platform dashboard statistics successfully")
    void testGetPlatformDashboard_Success() {
        // Arrange: Mock competition list
        CompetitionResponseVO comp1 = new CompetitionResponseVO();
        comp1.setParticipationType(ParticipationType.INDIVIDUAL);
        comp1.setStatus(CompetitionStatus.ONGOING);

        CompetitionResponseVO comp2 = new CompetitionResponseVO();
        comp2.setParticipationType(ParticipationType.TEAM);
        comp2.setStatus(CompetitionStatus.COMPLETED);

        when(competitionServiceClient.listAllCompetitions())
                .thenReturn(ResponseEntity.ok(List.of(comp1, comp2)));

        // Arrange: Mock platform participant statistics
        PlatformParticipantStatisticsVO participantStats = new PlatformParticipantStatisticsVO();
        participantStats.setTotalParticipants(1000);
        participantStats.setIndividualParticipants(700);
        participantStats.setTeamParticipants(300);
        when(registrationServiceClient.getPlatformParticipantStatistics())
                .thenReturn(ResponseEntity.ok(participantStats));

        // Arrange: Mock platform submission statistics
        PlatformSubmissionStatisticsVO submissionStats = new PlatformSubmissionStatisticsVO();
        submissionStats.setTotalSubmissions(500);
        submissionStats.setApprovedSubmissions(400);
        submissionStats.setIndividualSubmissions(300);
        submissionStats.setTeamSubmissions(200);
        when(registrationServiceClient.getPlatformSubmissionStatistics())
                .thenReturn(ResponseEntity.ok(submissionStats));

        // Arrange: Mock interaction statistics
        InteractionStatisticsVO interactionStats = new InteractionStatisticsVO();
        interactionStats.setVoteCount(600L);
        interactionStats.setCommentCount(150L);
        when(interactionServiceClient.getPlatformInteractionStatistics())
                .thenReturn(ResponseEntity.ok(interactionStats));

        when(registrationServiceClient.getPlatformParticipantTrend())
                .thenReturn(ResponseEntity.ok(Map.of(
                        "individual", Map.of("2025-01", 100)
                )));
        when(registrationServiceClient.getPlatformSubmissionTrend())
                .thenReturn(ResponseEntity.ok(Map.of(
                        "2025-01", 80
                )));

        // Act
        PlatformDashboardVO dashboard = dashboardService.getPlatformDashboard();

        // Assert
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalCompetitions()).isEqualTo(2);
        assertThat(dashboard.getIndividualCompetitions()).isEqualTo(1);
        assertThat(dashboard.getTeamCompetitions()).isEqualTo(1);
        assertThat(dashboard.getActiveCompetitions()).isEqualTo(1);
        assertThat(dashboard.getFinishedCompetitions()).isEqualTo(1);
        assertThat(dashboard.getTotalParticipants()).isEqualTo(1000);
        assertThat(dashboard.getIndividualParticipants()).isEqualTo(700);
        assertThat(dashboard.getTeamParticipants()).isEqualTo(300);
        assertThat(dashboard.getTotalSubmissions()).isEqualTo(500);
        assertThat(dashboard.getApprovedSubmissions()).isEqualTo(400);
        assertThat(dashboard.getTotalVotes()).isEqualTo(600);
        assertThat(dashboard.getTotalComments()).isEqualTo(150);
        assertThat(dashboard.getParticipantTrend()).isNotEmpty();
        assertThat(dashboard.getSubmissionTrend()).isNotEmpty();
    }

}
