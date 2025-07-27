package com.w16a.danish.judge.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.vo.SubmissionInfoVO;
import com.w16a.danish.judge.domain.vo.SubmissionScoreStatisticsVO;
import com.w16a.danish.judge.mapper.SubmissionRecordsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubmissionRecordsServiceImplTest {

    @Mock
    private SubmissionRecordsMapper submissionRecordsMapper;

    @Spy
    @InjectMocks
    private SubmissionRecordsServiceImpl submissionRecordsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Should calculate correct statistics when submissions exist")
    void testGetSubmissionScoreStatistics_WithData() {
        // Arrange
        SubmissionRecords record1 = new SubmissionRecords().setTotalScore(BigDecimal.valueOf(80));
        SubmissionRecords record2 = new SubmissionRecords().setTotalScore(BigDecimal.valueOf(90));
        SubmissionRecords record3 = new SubmissionRecords().setTotalScore(BigDecimal.valueOf(100));

        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery);
        when(fakeQuery.isNotNull(any())).thenReturn(fakeQuery);
        when(fakeQuery.list()).thenReturn(List.of(record1, record2, record3));

        // Act
        SubmissionScoreStatisticsVO stats = submissionRecordsService.getSubmissionScoreStatistics("comp-id");

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getAverageScore()).isEqualByComparingTo("90.00");
        assertThat(stats.getHighestScore()).isEqualByComparingTo("100");
        assertThat(stats.getLowestScore()).isEqualByComparingTo("80");
    }

    @Test
    @DisplayName("✅ Should return empty statistics if no submissions found")
    void testGetSubmissionScoreStatistics_NoData() {
        // Arrange
        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery);
        when(fakeQuery.isNotNull(any())).thenReturn(fakeQuery);
        when(fakeQuery.list()).thenReturn(Collections.emptyList());

        // Act
        SubmissionScoreStatisticsVO stats = submissionRecordsService.getSubmissionScoreStatistics("comp-id");

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getAverageScore()).isNull();
        assertThat(stats.getHighestScore()).isNull();
        assertThat(stats.getLowestScore()).isNull();
    }

    @Test
    @DisplayName("✅ Should get my submission basic info successfully")
    void testGetMySubmissionBasic_Found() {
        // Arrange
        SubmissionRecords record = new SubmissionRecords()
                .setId("submission-1")
                .setCompetitionId("comp-id")
                .setUserId("user-id")
                .setTitle("Test Submission")
                .setTotalScore(BigDecimal.valueOf(95));

        // Mock the query chain
        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery);    // first eq
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery);    // second eq
        when(fakeQuery.select(any(SFunction[].class))).thenReturn(fakeQuery); // ⭐ select mock correctly
        when(fakeQuery.one()).thenReturn(record);  // finally one()

        // Act
        SubmissionInfoVO vo = submissionRecordsService.getMySubmissionBasic("comp-id", "user-id");

        // Assert
        assertThat(vo).isNotNull();
        assertThat(vo.getId()).isEqualTo("submission-1");
        assertThat(vo.getTitle()).isEqualTo("Test Submission");
        assertThat(vo.getTotalScore()).isEqualByComparingTo("95");
    }

    @Test
    @DisplayName("❌ Should return null if no my submission found")
    void testGetMySubmissionBasic_NotFound() {
        // Arrange
        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // first eq
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // second eq
        when(fakeQuery.select(any(SFunction[].class))).thenReturn(fakeQuery); // ⭐ select chain
        when(fakeQuery.one()).thenReturn(null); // no record found

        // Act
        SubmissionInfoVO vo = submissionRecordsService.getMySubmissionBasic("comp-id", "user-id");

        // Assert
        assertThat(vo).isNull();
    }

    @Test
    @DisplayName("✅ Should get team submission basic info successfully")
    void testGetTeamSubmissionBasic_Found() {
        // Arrange
        SubmissionRecords record = new SubmissionRecords()
                .setId("submission-2")
                .setCompetitionId("comp-id")
                .setTeamId("team-id")
                .setTitle("Team Project");

        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // first eq
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // second eq
        when(fakeQuery.select(any(SFunction[].class))).thenReturn(fakeQuery); // ⭐ fix select chain
        when(fakeQuery.one()).thenReturn(record); // mock result

        // Act
        SubmissionInfoVO vo = submissionRecordsService.getTeamSubmissionBasic("comp-id", "team-id");

        // Assert
        assertThat(vo).isNotNull();
        assertThat(vo.getId()).isEqualTo("submission-2");
        assertThat(vo.getTitle()).isEqualTo("Team Project");
    }

    @Test
    @DisplayName("❌ Should return null if no team submission found")
    void testGetTeamSubmissionBasic_NotFound() {
        // Arrange
        var fakeQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(fakeQuery).when(submissionRecordsService).lambdaQuery();
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // first eq
        when(fakeQuery.eq(any(), any())).thenReturn(fakeQuery); // second eq
        when(fakeQuery.select(any(SFunction[].class))).thenReturn(fakeQuery); // ⭐ fix select chain
        when(fakeQuery.one()).thenReturn(null); // no record found

        // Act
        SubmissionInfoVO vo = submissionRecordsService.getTeamSubmissionBasic("comp-id", "team-id");

        // Assert
        assertThat(vo).isNull();
    }

}
