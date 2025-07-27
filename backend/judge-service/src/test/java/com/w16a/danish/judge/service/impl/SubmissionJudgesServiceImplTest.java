package com.w16a.danish.judge.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.judge.domain.dto.CriterionScoreDTO;
import com.w16a.danish.judge.domain.dto.SubmissionJudgeDTO;
import com.w16a.danish.judge.domain.enums.CompetitionStatus;
import com.w16a.danish.judge.domain.po.CompetitionJudges;
import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.w16a.danish.judge.domain.po.SubmissionJudges;
import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.vo.*;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.SubmissionServiceClient;
import com.w16a.danish.judge.mapper.SubmissionJudgesMapper;
import com.w16a.danish.judge.service.ICompetitionJudgesService;
import com.w16a.danish.judge.service.ISubmissionJudgeScoresService;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SubmissionJudgesServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionJudgesServiceImpl submissionJudgesService;

    @Mock private ICompetitionJudgesService competitionJudgesService;
    @Mock private ISubmissionJudgeScoresService submissionJudgeScoresService;
    @Mock private CompetitionServiceClient competitionServiceClient;
    @Mock private SubmissionServiceClient submissionServiceClient;
    @Mock private ISubmissionRecordsService submissionRecordsService;
    @Mock private SubmissionJudgesMapper submissionJudgesMapper;


    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // 👉 Inject mocked submissionJudgesMapper to baseMapper field
        var baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(submissionJudgesService, submissionJudgesMapper);
    }

    @Test
    @DisplayName("✅ Should judge a submission successfully")
    void testJudgeSubmission_Success() throws Exception {
        // Mock judge assignment
        LambdaQueryChainWrapper<CompetitionJudges> judgeAssignmentQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeAssignmentQuery).when(competitionJudgesService).lambdaQuery();
        when(judgeAssignmentQuery.eq(any(), any())).thenReturn(judgeAssignmentQuery);
        when(judgeAssignmentQuery.exists()).thenReturn(true);

        // Mock no previous judging
        LambdaQueryChainWrapper<SubmissionJudges> judgedQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgedQuery).when(submissionJudgesService).lambdaQuery();
        when(judgedQuery.eq(any(), any())).thenReturn(judgedQuery);
        when(judgedQuery.exists()).thenReturn(false);

        // Mock competition info
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(mockCompetitionCompleted()));

        // Mock saving judge record
        when(submissionJudgesService.save(any())).thenReturn(true);

        // Mock saving judge scores
        when(submissionJudgeScoresService.saveBatch(any())).thenReturn(true);

        // Mock updating submission total score
        LambdaUpdateChainWrapper<SubmissionRecords> updateWrapper = mock(LambdaUpdateChainWrapper.class);
        when(submissionRecordsService.lambdaUpdate()).thenReturn(updateWrapper);
        when(updateWrapper.eq(any(), any())).thenReturn(updateWrapper);
        when(updateWrapper.set(any(), any())).thenReturn(updateWrapper);
        when(updateWrapper.update()).thenReturn(true);

        LambdaQueryChainWrapper<SubmissionJudges> scoreQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(scoreQuery).when(submissionJudgesService).lambdaQuery();
        when(scoreQuery.eq(any(), any())).thenReturn(scoreQuery);
        when(scoreQuery.select(any(SFunction.class))).thenReturn(scoreQuery);
        when(scoreQuery.list()).thenReturn(List.of(
                new SubmissionJudges().setTotalScore(BigDecimal.valueOf(4))
        ));

        // Act + Assert
        SubmissionJudgeDTO judgeDTO = buildJudgeDTO();
        assertThatCode(() -> submissionJudgesService.judgeSubmission("judge-1", judgeDTO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should check user assigned as judge")
    void testIsUserAssignedAsJudge_Success() {
        LambdaQueryChainWrapper<CompetitionJudges> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(competitionJudgesService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(mockCompetitionCompleted()));

        assertThat(submissionJudgesService.isUserAssignedAsJudge("user-1", "comp-1")).isTrue();
    }

    @Test
    @DisplayName("✅ Should get my judging detail successfully")
    void testGetMyJudgingDetail_Success() {
        LambdaQueryChainWrapper<SubmissionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeQuery).when(submissionJudgesService).lambdaQuery();
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        when(judgeQuery.one()).thenReturn(new SubmissionJudges().setSubmissionId("submission-1"));

        LambdaQueryChainWrapper<SubmissionJudgeScores> scoreQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(scoreQuery).when(submissionJudgeScoresService).lambdaQuery();
        when(scoreQuery.eq(any(), any())).thenReturn(scoreQuery);
        when(scoreQuery.list()).thenReturn(List.of(new SubmissionJudgeScores()));

        SubmissionJudgeVO vo = submissionJudgesService.getMyJudgingDetail("judge-1", "submission-1");

        assertThat(vo).isNotNull();
        assertThat(vo.getSubmissionId()).isEqualTo("submission-1");
    }

    @Test
    @DisplayName("✅ Should list pending submissions for judging successfully")
    void testListPendingSubmissionsForJudging_Success() {
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(mockCompetitionCompleted()));

        when(submissionServiceClient.listApprovedSubmissionsPublic(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(mockPageSubmissions()));

        LambdaQueryChainWrapper<SubmissionJudges> judgedQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgedQuery).when(submissionJudgesService).lambdaQuery();
        when(judgedQuery.eq(any(), any())).thenReturn(judgedQuery);
        when(judgedQuery.select(any(SFunction.class))).thenReturn(judgedQuery);
        when(judgedQuery.list()).thenReturn(Collections.emptyList());

        PageResponse<SubmissionBriefVO> page = submissionJudgesService.listPendingSubmissionsForJudging(
                "judge-1", "comp-1", null, "asc", 1, 10
        );

        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotEmpty();
    }

    @Test
    @DisplayName("✅ Should list my judging competitions successfully")
    void testListMyJudgingCompetitions_Success() {
        LambdaQueryChainWrapper<CompetitionJudges> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(competitionJudgesService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.select(any(SFunction.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of(new CompetitionJudges().setCompetitionId("comp-1")));

        when(competitionServiceClient.getCompetitionsByIds(anyList()))
                .thenReturn(ResponseEntity.ok(List.of(mockCompetitionCompleted())));

        PageResponse<CompetitionResponseVO> page = submissionJudgesService.listMyJudgingCompetitions(
                "judge-1", null, "createdAt", "asc", 1, 10
        );

        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotEmpty();
    }

    @Test
    @DisplayName("❌ Should throw exception if user is not assigned as judge")
    void testJudgeSubmission_NotAssignedJudge() {
        LambdaQueryChainWrapper<CompetitionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeQuery).when(competitionJudgesService).lambdaQuery();
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        when(judgeQuery.exists()).thenReturn(false);

        SubmissionJudgeDTO judgeDTO = buildJudgeDTO();
        assertThatThrownBy(() -> submissionJudgesService.judgeSubmission("judge-1", judgeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You are not assigned as a judge");
    }

    @Test
    @DisplayName("❌ Should throw exception if already judged")
    void testJudgeSubmission_AlreadyJudged() {
        LambdaQueryChainWrapper<CompetitionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeQuery).when(competitionJudgesService).lambdaQuery();
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        when(judgeQuery.exists()).thenReturn(true);

        LambdaQueryChainWrapper<SubmissionJudges> judgedQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgedQuery).when(submissionJudgesService).lambdaQuery();
        when(judgedQuery.eq(any(), any())).thenReturn(judgedQuery);
        when(judgedQuery.exists()).thenReturn(true);

        SubmissionJudgeDTO judgeDTO = buildJudgeDTO();
        assertThatThrownBy(() -> submissionJudgesService.judgeSubmission("judge-1", judgeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You have already judged");
    }

    @Test
    @DisplayName("❌ Should throw exception if no existing judgement when updating")
    void testUpdateJudgement_NoExistingJudgement() {
        LambdaQueryChainWrapper<SubmissionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(judgeQuery).when(submissionJudgesService).lambdaQuery();
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        when(judgeQuery.one()).thenReturn(null);

        SubmissionJudgeDTO judgeDTO = buildJudgeDTO();
        assertThatThrownBy(() -> submissionJudgesService.updateJudgement("judge-1", "submission-1", judgeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No existing judging record found");
    }

    @Test
    @DisplayName("✅ Should handle empty approved submissions")
    void testListPendingSubmissionsForJudging_EmptyApprovedSubmissions() {
        when(competitionServiceClient.getCompetitionById(any()))
                .thenReturn(ResponseEntity.ok(mockCompetitionCompleted()));

        when(submissionServiceClient.listApprovedSubmissionsPublic(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(PageResponse.<SubmissionInfoVO>builder()
                        .data(Collections.emptyList())
                        .page(1).size(10).total(0L).pages(1).build()));

        PageResponse<SubmissionBriefVO> page = submissionJudgesService.listPendingSubmissionsForJudging(
                "judge-1", "comp-1", null, "asc", 1, 10
        );

        assertThat(page).isNotNull();
        assertThat(page.getData()).isEmpty();
    }

    @Test
    @DisplayName("✅ Should handle empty competition list when listing my judging competitions")
    void testListMyJudgingCompetitions_EmptyCompetitions() {
        LambdaQueryChainWrapper<CompetitionJudges> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(competitionJudgesService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.select(any(SFunction.class))).thenReturn(query);
        when(query.list()).thenReturn(Collections.emptyList());

        when(competitionServiceClient.getCompetitionsByIds(anyList()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        PageResponse<CompetitionResponseVO> page = submissionJudgesService.listMyJudgingCompetitions(
                "judge-1", null, "createdAt", "asc", 1, 10
        );

        assertThat(page).isNotNull();
        assertThat(page.getData()).isEmpty();
    }

    // ----------- Helper methods -----------

    private SubmissionJudgeDTO buildJudgeDTO() {
        CriterionScoreDTO criterion = new CriterionScoreDTO();
        criterion.setCriterion("Creativity");
        criterion.setScore(BigDecimal.valueOf(4));
        criterion.setWeight(BigDecimal.valueOf(0.5));

        SubmissionJudgeDTO dto = new SubmissionJudgeDTO();
        dto.setCompetitionId("comp-1");
        dto.setSubmissionId("submission-1");
        dto.setJudgeComments("Good job");
        dto.setScores(List.of(criterion));
        return dto;
    }

    private CompetitionResponseVO mockCompetitionCompleted() {
        CompetitionResponseVO vo = new CompetitionResponseVO();
        vo.setStatus(CompetitionStatus.COMPLETED);
        vo.setEndDate(LocalDateTime.now().minusDays(1));
        return vo;
    }

    private PageResponse<SubmissionInfoVO> mockPageSubmissions() {
        SubmissionInfoVO vo = new SubmissionInfoVO();
        vo.setId("submission-1");
        vo.setTitle("Test Submission");
        vo.setFileName("submission.pdf");
        vo.setCreatedAt(LocalDateTime.now().minusDays(1));
        return PageResponse.<SubmissionInfoVO>builder()
                .data(List.of(vo))
                .page(1)
                .size(10)
                .total(1L)
                .pages(1)
                .build();
    }
}
