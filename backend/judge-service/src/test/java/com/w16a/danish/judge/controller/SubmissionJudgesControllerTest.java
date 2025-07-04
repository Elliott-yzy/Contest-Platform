package com.w16a.danish.judge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.judge.domain.dto.CriterionScoreDTO;
import com.w16a.danish.judge.domain.dto.SubmissionJudgeDTO;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.SubmissionJudgeVO;
import com.w16a.danish.judge.service.ISubmissionJudgesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link SubmissionJudgesController}.
 * Covers all judging APIs including scoring, updating, checking assignments, and listing submissions.
 * Author: Eddy Zhang
 * Date: 2025/04/27
 */
@SpringBootTest
@AutoConfigureMockMvc
class SubmissionJudgesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISubmissionJudgesService submissionJudgesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Judge a submission successfully")
    void testJudgeSubmission() throws Exception {
        // Arrange
        CriterionScoreDTO criterionScore = new CriterionScoreDTO();
        criterionScore.setCriterion("Creativity");
        criterionScore.setScore(new BigDecimal("8.5"));
        criterionScore.setWeight(new BigDecimal("0.4"));

        SubmissionJudgeDTO dto = new SubmissionJudgeDTO();
        dto.setCompetitionId("comp-id");
        dto.setSubmissionId("sub-id");
        dto.setJudgeComments("Excellent innovation with minor improvements needed.");
        dto.setScores(List.of(criterionScore));

        // Act & Assert
        mockMvc.perform(post("/judges/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "judge-user-id")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Submission judged successfully."));
    }

    @Test
    @DisplayName("✅ Check if user is assigned as judge successfully")
    void testIsAssignedJudge() throws Exception {
        when(submissionJudgesService.isUserAssignedAsJudge(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/judges/is-judge")
                        .header("User-ID", "judge-user-id")
                        .param("competitionId", "comp-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Get judging detail successfully")
    void testGetMyJudgingDetail() throws Exception {
        when(submissionJudgesService.getMyJudgingDetail(anyString(), anyString()))
                .thenReturn(new SubmissionJudgeVO());

        mockMvc.perform(get("/judges/{submissionId}/detail", "sub-id")
                        .header("User-ID", "judge-user-id"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("✅ List pending submissions for judging")
    void testListPendingSubmissionsForJudging() throws Exception {
        when(submissionJudgesService.listPendingSubmissionsForJudging(anyString(), anyString(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/judges/pending-submissions")
                        .header("User-ID", "judge-user-id")
                        .param("competitionId", "comp-id")
                        .param("keyword", "AI")
                        .param("sortOrder", "desc")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("✅ Update judgement for a submission successfully")
    void testUpdateJudgement() throws Exception {
        // Arrange
        CriterionScoreDTO criterionScore = new CriterionScoreDTO();
        criterionScore.setCriterion("Technical Excellence"); // Set criterion name
        criterionScore.setScore(new BigDecimal("9.0"));       // Set score using BigDecimal
        criterionScore.setWeight(new BigDecimal("0.5"));      // Set weight using BigDecimal

        SubmissionJudgeDTO dto = new SubmissionJudgeDTO();
        dto.setCompetitionId("comp-id");                      // Set competition ID
        dto.setSubmissionId("sub-id");                        // Set submission ID
        dto.setJudgeComments("Updated after review.");        // Set judge comments
        dto.setScores(List.of(criterionScore));               // Add scoring items

        // Act & Assert
        mockMvc.perform(put("/judges/{submissionId}", "sub-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "judge-user-id")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Judging updated successfully."));
    }

    @Test
    @DisplayName("✅ List competitions where the user is assigned as judge")
    void testListMyJudgingCompetitions() throws Exception {
        when(submissionJudgesService.listMyJudgingCompetitions(anyString(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/judges/my-competitions")
                        .header("User-ID", "judge-user-id")
                        .param("keyword", "Hackathon")
                        .param("sortBy", "createdAt")
                        .param("order", "desc")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
