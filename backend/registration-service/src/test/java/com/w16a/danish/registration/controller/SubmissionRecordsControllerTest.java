package com.w16a.danish.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.registration.domain.dto.SubmissionReviewDTO;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.service.ISubmissionRecordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ✅ Unit tests for SubmissionRecordsController.
 * Focus on verifying API endpoints behavior without real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmissionRecordsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISubmissionRecordsService submissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Upload submission work successfully")
    void testUploadSubmission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "content".getBytes());

        doNothing().when(submissionService).submitWork(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/submissions/upload")
                        .file(file)
                        .param("competitionId", "comp-1")
                        .param("title", "Test Title")
                        .param("description", "Test Description")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Delete submission successfully")
    void testDeleteSubmission() throws Exception {
        doNothing().when(submissionService).deleteSubmission(any(), any(), any());

        mockMvc.perform(delete("/submissions/{submissionId}", "sub-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ View submitted work successfully")
    void testGetMySubmission() throws Exception {
        when(submissionService.getMySubmission(any(), any(), any()))
                .thenReturn(new SubmissionInfoVO());

        mockMvc.perform(get("/submissions/{competitionId}", "comp-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List submissions for a competition successfully")
    void testListSubmissionsForCompetition() throws Exception {
        when(submissionService.listSubmissionsByRole(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/submissions/public")
                        .param("competitionId", "comp-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List approved public submissions successfully")
    void testListApprovedSubmissionsPublic() throws Exception {
        when(submissionService.listPublicApprovedSubmissions(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/submissions/public/approved")
                        .param("competitionId", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Review a submission successfully")
    void testReviewSubmission() throws Exception {
        SubmissionReviewDTO dto = new SubmissionReviewDTO();

        doNothing().when(submissionService).reviewSubmission(any(), any(), any());

        mockMvc.perform(post("/submissions/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "user-1")
                        .header("User-Role", "ORGANIZER")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Check if user is organizer of a submission successfully")
    void testIsUserOrganizerOfSubmission() throws Exception {
        when(submissionService.isUserOrganizerOfSubmission(any(), any()))
                .thenReturn(true);

        mockMvc.perform(get("/submissions/is-organizer")
                        .param("submissionId", "sub-1")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Upload team submission successfully")
    void testUploadTeamSubmission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "teamfile.txt", "text/plain", "team content".getBytes());

        doNothing().when(submissionService).submitTeamWork(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/submissions/teams/upload")
                        .file(file)
                        .param("competitionId", "comp-1")
                        .param("teamId", "team-1")
                        .param("title", "Team Title")
                        .param("description", "Team Description")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ View public team submission successfully")
    void testGetTeamSubmissionPublic() throws Exception {
        when(submissionService.getTeamSubmissionPublic(any(), any()))
                .thenReturn(new TeamSubmissionInfoVO());

        mockMvc.perform(get("/submissions/public/teams/{competitionId}/{teamId}", "comp-1", "team-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Delete team submission successfully")
    void testDeleteTeamSubmission() throws Exception {
        doNothing().when(submissionService).deleteTeamSubmission(any(), any(), any());

        mockMvc.perform(delete("/submissions/teams/{submissionId}", "sub-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List all team submissions successfully")
    void testListTeamSubmissions() throws Exception {
        when(submissionService.listTeamSubmissionsByRole(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/submissions/teams/list")
                        .param("competitionId", "comp-1")
                        .header("User-ID", "organizer-1")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List approved team submissions publicly successfully")
    void testListApprovedTeamSubmissionsPublic() throws Exception {
        when(submissionService.listPublicApprovedTeamSubmissions(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/submissions/public/teams/approved")
                        .param("competitionId", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Check if submission exists by team successfully")
    void testExistsByTeamId() throws Exception {
        when(submissionService.existsByTeamId(any()))
                .thenReturn(true);

        mockMvc.perform(get("/submissions/internal/exists-by-team")
                        .param("teamId", "team-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Get competition submission statistics successfully")
    void testGetSubmissionStatistics() throws Exception {
        when(submissionService.getSubmissionStatistics(any()))
                .thenReturn(new SubmissionStatisticsVO());

        mockMvc.perform(get("/submissions/statistics")
                        .param("competitionId", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get public competition submission trend successfully")
    void testGetSubmissionTrend() throws Exception {
        when(submissionService.getSubmissionTrend(any()))
                .thenReturn(Map.of());

        mockMvc.perform(get("/submissions/public/{competitionId}/submission-trend", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get platform submission statistics successfully")
    void testGetPlatformSubmissionStatistics() throws Exception {
        when(submissionService.getPlatformSubmissionStatistics())
                .thenReturn(new PlatformSubmissionStatisticsVO());

        mockMvc.perform(get("/submissions/public/platform/submission-statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get platform submission trend successfully")
    void testGetPlatformSubmissionTrend() throws Exception {
        when(submissionService.getPlatformSubmissionTrend())
                .thenReturn(Map.of());

        mockMvc.perform(get("/submissions/public/platform/submission-trend"))
                .andExpect(status().isOk());
    }
}
