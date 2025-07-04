package com.w16a.danish.judge.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.judge.config.AwardNotifier;
import com.w16a.danish.judge.domain.mq.AwardWinnerMessage;
import com.w16a.danish.judge.domain.po.SubmissionJudges;
import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.po.SubmissionWinners;
import com.w16a.danish.judge.domain.vo.CompetitionResponseVO;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.UserServiceClient;
import com.w16a.danish.judge.mapper.SubmissionWinnersMapper;
import com.w16a.danish.judge.service.ISubmissionJudgeScoresService;
import com.w16a.danish.judge.service.ISubmissionJudgesService;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SubmissionWinnersServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionWinnersServiceImpl winnersService;

    @Mock private CompetitionServiceClient competitionServiceClient;
    @Mock private ISubmissionRecordsService submissionRecordsService;
    @Mock private ISubmissionJudgeScoresService submissionJudgeScoresService;
    @Mock private ISubmissionJudgesService submissionJudgesService;
    @Mock private UserServiceClient userServiceClient;
    @Mock private AwardNotifier awardNotifier;
    @Mock private SubmissionWinnersMapper submissionWinnersMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        var baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(winnersService, submissionWinnersMapper);
    }

    @Test
    @DisplayName("✅ Should list scored submissions successfully")
    void testListScoredSubmissionsSuccess() {
        // Arrange - Mock permission check
        when(competitionServiceClient.isUserOrganizer(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(true));

        // Arrange - Mock submission records
        LambdaQueryChainWrapper<SubmissionRecords> recordQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(recordQuery).when(submissionRecordsService).lambdaQuery();
        when(recordQuery.eq(any(SFunction.class), any())).thenReturn(recordQuery);
        when(recordQuery.isNotNull(any(SFunction.class))).thenReturn(recordQuery);
        when(recordQuery.in(any(SFunction.class), anyCollection())).thenReturn(recordQuery);
        when(recordQuery.list()).thenReturn(List.of(
                new SubmissionRecords()
                        .setId("submission-1")
                        .setTitle("Awesome Project")
                        .setTotalScore(BigDecimal.valueOf(95))
        ));

        LambdaQueryChainWrapper<SubmissionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeQuery).when(submissionJudgesService).lambdaQuery();
        when(judgeQuery.in(any(SFunction.class), anyCollection())).thenReturn(judgeQuery);
        when(judgeQuery.select(any(SFunction.class))).thenReturn(judgeQuery);
        when(judgeQuery.list()).thenReturn(List.of(
                new SubmissionJudges().setSubmissionId("submission-1"),
                new SubmissionJudges().setSubmissionId("submission-1"),
                new SubmissionJudges().setSubmissionId("submission-1")
        ));

        // Arrange - Mock submission scores (empty is OK)
        when(submissionJudgeScoresService.listBySubmissionIds(anyList()))
                .thenReturn(Collections.emptyList());

        // Act
        PageResponse<?> response = winnersService.listScoredSubmissions(
                "userId", "ORGANIZER", "comp-id", null, "totalScore", "desc", 1, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("❌ Should throw forbidden when listing scored submissions by non-organizer")
    void testListScoredSubmissionsForbidden() {
        when(competitionServiceClient.isUserOrganizer(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(false));

        assertThatThrownBy(() -> winnersService.listScoredSubmissions(
                "userId", "PARTICIPANT", "comp-id", null, "totalScore", "desc", 1, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only organizers or admins can view scored submissions");
    }

    @Test
    @DisplayName("✅ Should auto award successfully")
    void testAutoAwardSuccess() {
        // Mock permission: organizer or admin
        when(competitionServiceClient.isUserOrganizer(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(true));

        // Mock submissionRecordsService.lambdaQuery() -> returning one submission record
        LambdaQueryChainWrapper<SubmissionRecords> recordQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(recordQuery).when(submissionRecordsService).lambdaQuery();
        when(recordQuery.eq(any(), any())).thenReturn(recordQuery);
        when(recordQuery.isNotNull(any())).thenReturn(recordQuery);
        when(recordQuery.list()).thenReturn(List.of(
                new SubmissionRecords()
                        .setId("submission-1")
                        .setTotalScore(BigDecimal.valueOf(90))
                        .setUserId("user-1")
        ));

        // Mock no criterion scores
        when(submissionJudgeScoresService.listBySubmissionIds(anyList()))
                .thenReturn(Collections.emptyList());

        // Mock saveBatch to succeed
        doReturn(true).when(winnersService).saveBatch(anyList());

        // Mock competition status update
        when(competitionServiceClient.updateCompetitionStatus(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(new CompetitionResponseVO()));

        // Mock getCompetitionById to avoid NPE
        CompetitionResponseVO mockCompetition = new CompetitionResponseVO();
        mockCompetition.setName("Mocked Competition");
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(mockCompetition));

        // Mock userServiceClient.getUserBriefById to avoid NPE
        var mockUser = new com.w16a.danish.judge.domain.vo.UserBriefVO();
        mockUser.setId("user-1");
        mockUser.setName("Mocked User");
        mockUser.setEmail("mockeduser@example.com");
        when(userServiceClient.getUserBriefById(anyString()))
                .thenReturn(ResponseEntity.ok(mockUser));

        // Mock awardNotifier to do nothing
        doNothing().when(awardNotifier).sendAwardWinner(any());

        // Act
        winnersService.autoAward("userId", "ADMIN", "comp-id");

        // Assert: Verify critical interactions
        verify(winnersService, times(1)).saveBatch(anyList());
        verify(competitionServiceClient, times(1)).updateCompetitionStatus(anyString(), anyString());
        verify(awardNotifier, atLeastOnce()).sendAwardWinner(any());
    }

    @Test
    @DisplayName("❌ Should throw forbidden when auto awarding by non-organizer")
    void testAutoAwardForbidden() {
        when(competitionServiceClient.isUserOrganizer(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(false));

        assertThatThrownBy(() -> winnersService.autoAward("userId", "PARTICIPANT", "comp-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only organizers or admins can auto-award");
    }

    @Test
    @DisplayName("✅ Should list public winners successfully")
    void testListPublicWinnersSuccess() {
        // Mock winnersService.lambdaQuery()
        LambdaQueryChainWrapper<SubmissionWinners> winnerQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(winnerQuery).when(winnersService).lambdaQuery();
        when(winnerQuery.eq(any(SFunction.class), any())).thenReturn(winnerQuery);
        when(winnerQuery.list()).thenReturn(List.of(
                new SubmissionWinners()
                        .setSubmissionId("submission-1")
                        .setAwardName("Champion")
        ));

        // Mock submissionRecordsService.lambdaQuery()
        LambdaQueryChainWrapper<SubmissionRecords> recordQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(recordQuery).when(submissionRecordsService).lambdaQuery();
        when(recordQuery.in(any(SFunction.class), anyCollection())).thenReturn(recordQuery);
        when(recordQuery.list()).thenReturn(List.of(
                new SubmissionRecords()
                        .setId("submission-1")
                        .setTitle("Innovation Project")
                        .setTotalScore(BigDecimal.valueOf(88))
        ));

        when(userServiceClient.getUsersByIds(anyList(), any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(userServiceClient.getTeamBriefByIds(anyList()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        PageResponse<?> response = winnersService.listPublicWinners("comp-id", 1, 10);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    @DisplayName("❌ Should not send notification if competition not found")
    void testSendAwardNotification_CompetitionNotFound() throws Exception {
        // Arrange
        SubmissionRecords submission = new SubmissionRecords()
                .setId("submission-1")
                .setUserId("user-1");

        // Mock: getCompetitionById returns ResponseEntity.ok(null)
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(null));

        // Reflectively call private sendAwardNotification() method
        Method method = SubmissionWinnersServiceImpl.class.getDeclaredMethod(
                "sendAwardNotification",
                SubmissionRecords.class,
                String.class,
                List.class
        );
        method.setAccessible(true);

        // Act
        method.invoke(winnersService, submission, "comp-id", List.of());

        // Assert: No exception should be thrown and no notifications sent
        verifyNoInteractions(userServiceClient);
        verifyNoInteractions(awardNotifier);
    }

    @Test
    @DisplayName("❌ Should not send notification if no recipients found")
    void testSendAwardNotification_NoRecipients() throws Exception {
        // Arrange
        SubmissionRecords submission = new SubmissionRecords()
                .setId("submission-1")
                .setUserId("user-1");

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setName("Mocked Competition");

        // Mock competitionServiceClient.getCompetitionById returns a valid competition
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(competition));

        // Mock userServiceClient.getUserBriefById returns empty (simulate no recipient found)
        when(userServiceClient.getUserBriefById(anyString()))
                .thenReturn(ResponseEntity.ok(null));

        // Access private sendAwardNotification method via reflection
        Method method = SubmissionWinnersServiceImpl.class.getDeclaredMethod(
                "sendAwardNotification",
                SubmissionRecords.class,
                String.class,
                List.class
        );
        method.setAccessible(true);

        // Act
        method.invoke(winnersService, submission, "comp-id", List.of());

        // Assert
        verify(awardNotifier, never()).sendAwardWinner(any());
    }

    @Test
    @DisplayName("✅ Should build award message correctly for a winner")
    void testBuildAwardMessage_Winner() {
        // Arrange
        String userName = "Mocked User";
        String userEmail = "mocked@example.com";

        SubmissionRecords submission = new SubmissionRecords()
                .setId("submission-1");

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setName("Mocked Competition");

        List<SubmissionWinners> winners = List.of(
                new SubmissionWinners()
                        .setSubmissionId("submission-1")
                        .setAwardName("Champion")
        );

        // Act: Use ReflectionTestUtils to call private method
        var message = (AwardWinnerMessage) ReflectionTestUtils.invokeMethod(
                winnersService,
                "buildAwardMessage",
                userName,
                userEmail,
                submission,
                competition,
                true,    // isWinner
                winners
        );

        // Assert
        assertThat(message).isNotNull();
        assertThat(message.getUserName()).isEqualTo(userName);
        assertThat(message.getUserEmail()).isEqualTo(userEmail);
        assertThat(message.getCompetitionName()).isEqualTo("Mocked Competition");
        assertThat(message.getAwardName()).isEqualTo("Champion");
        assertThat(message.getAwardedAt()).isNotNull(); // should have timestamp
    }

    @Test
    @DisplayName("✅ Should build award message correctly for a non-winner")
    void testBuildAwardMessage_NonWinner() {
        // Arrange
        String userName = "Mocked User";
        String userEmail = "mocked@example.com";

        SubmissionRecords submission = new SubmissionRecords()
                .setId("submission-1");

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setName("Mocked Competition");

        List<SubmissionWinners> winners = List.of(); // no winners

        // Act: Use ReflectionTestUtils to call private method
        var message = (AwardWinnerMessage) ReflectionTestUtils.invokeMethod(
                winnersService,
                "buildAwardMessage",
                userName,
                userEmail,
                submission,
                competition,
                false,   // isWinner
                winners
        );

        // Assert
        assertThat(message).isNotNull();
        assertThat(message.getUserName()).isEqualTo(userName);
        assertThat(message.getUserEmail()).isEqualTo(userEmail);
        assertThat(message.getCompetitionName()).isEqualTo("Mocked Competition");
        assertThat(message.getAwardName()).isEqualTo("None");
        assertThat(message.getAwardedAt()).isNotNull();
    }

}
