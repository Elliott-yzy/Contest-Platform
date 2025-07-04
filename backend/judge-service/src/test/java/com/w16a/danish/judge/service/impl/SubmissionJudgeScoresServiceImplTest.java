package com.w16a.danish.judge.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.w16a.danish.judge.mapper.SubmissionJudgeScoresMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubmissionJudgeScoresServiceImpl.
 */
class SubmissionJudgeScoresServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionJudgeScoresServiceImpl submissionJudgeScoresService;

    @Mock
    private SubmissionJudgeScoresMapper submissionJudgeScoresMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject mocked baseMapper into ServiceImpl (required for lambdaQuery to work)
        var baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(submissionJudgeScoresService, submissionJudgeScoresMapper);
    }

    @Test
    @DisplayName("✅ Should return empty list when submissionIds is null")
    void testListBySubmissionIds_NullInput() {
        List<SubmissionJudgeScores> result = submissionJudgeScoresService.listBySubmissionIds(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ Should return empty list when submissionIds is empty")
    void testListBySubmissionIds_EmptyInput() {
        List<SubmissionJudgeScores> result = submissionJudgeScoresService.listBySubmissionIds(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ Should return judge scores when submissionIds are valid")
    void testListBySubmissionIds_ValidInput() {
        // Arrange - mock lambdaQuery chain
        LambdaQueryChainWrapper<SubmissionJudgeScores> queryWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(queryWrapper).when(submissionJudgeScoresService).lambdaQuery();
        when(queryWrapper.in(any(), anyCollection())).thenReturn(queryWrapper);
        when(queryWrapper.list()).thenReturn(List.of(
                new SubmissionJudgeScores().setSubmissionId("submission-1")
        ));

        // Act
        List<SubmissionJudgeScores> result = submissionJudgeScoresService.listBySubmissionIds(List.of("submission-1"));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubmissionId()).isEqualTo("submission-1");
    }
}
