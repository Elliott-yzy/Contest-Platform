package com.w16a.danish.interaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.interaction.domain.dto.SubmissionCommentDTO;
import com.w16a.danish.interaction.domain.vo.PageResponse;
import com.w16a.danish.interaction.domain.vo.SubmissionCommentVO;
import com.w16a.danish.interaction.service.ISubmissionCommentsService;
import com.w16a.danish.interaction.service.ISubmissionVotesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmissionInteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISubmissionCommentsService commentsService;

    @MockitoBean
    private ISubmissionVotesService votesService;

    // === Test: Post Comment ===
    @Test
    @DisplayName("✅ Should add comment successfully")
    void testPostComment() throws Exception {
        SubmissionCommentDTO dto = new SubmissionCommentDTO();
        dto.setContent("Nice work!");
        dto.setSubmissionId("submission123");

        mockMvc.perform(post("/interactions/comments")
                        .header("User-ID", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment added successfully"));
    }

    // === Test: Delete Comment ===
    @Test
    @DisplayName("✅ Should delete comment successfully")
    void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/interactions/comments/{id}", "comment123")
                        .header("User-ID", "user123")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted"));
    }

    // === Test: Update Comment ===
    @Test
    @DisplayName("✅ Should update comment successfully")
    void testUpdateComment() throws Exception {
        SubmissionCommentDTO dto = new SubmissionCommentDTO();
        dto.setContent("Updated comment");

        mockMvc.perform(put("/interactions/comments/{id}", "comment123")
                        .header("User-ID", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment updated successfully"));
    }

    // === Test: Get Paginated Comments ===
    @Test
    @DisplayName("✅ Should get paginated comments successfully")
    void testGetComments() throws Exception {
        PageResponse<SubmissionCommentVO> mockPage = new PageResponse<>();
        mockPage.setData(Collections.emptyList());
        mockPage.setPage(1);
        mockPage.setSize(10);
        mockPage.setTotal(0L);

        Mockito.when(commentsService.getPaginatedComments(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPage);

        mockMvc.perform(get("/interactions/comments/list")
                        .param("submissionId", "submission123")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("order", "desc"))
                .andExpect(status().isOk());
    }


    // === Test: Vote ===
    @Test
    @DisplayName("✅ Should vote submission successfully")
    void testVote() throws Exception {
        mockMvc.perform(post("/interactions/votes")
                        .header("User-ID", "user123")
                        .param("submissionId", "submission123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Voted"));
    }

    // === Test: Unvote ===
    @Test
    @DisplayName("✅ Should unvote submission successfully")
    void testUnvote() throws Exception {
        mockMvc.perform(delete("/interactions/votes")
                        .header("User-ID", "user123")
                        .param("submissionId", "submission123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Unvoted"));
    }

    // === Test: Get Vote Count ===
    @Test
    @DisplayName("✅ Should get vote count successfully")
    void testGetVoteCount() throws Exception {
        Mockito.when(votesService.countVotes(anyString())).thenReturn(5L);

        mockMvc.perform(get("/interactions/votes/count")
                        .param("submissionId", "submission123"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    // === Test: Check Vote Status ===
    @Test
    @DisplayName("✅ Should check if user has voted")
    void testHasVoted() throws Exception {
        Mockito.when(votesService.hasVoted(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/interactions/votes/status")
                        .header("User-ID", "user123")
                        .param("submissionId", "submission123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // === Test: Get Submission Interaction Statistics ===
    @Test
    @DisplayName("✅ Should get submission interaction statistics successfully")
    void testGetInteractionStatistics() throws Exception {
        Mockito.when(votesService.countVotes(anyString())).thenReturn(10L);
        Mockito.when(commentsService.countComments(anyString())).thenReturn(3L);

        mockMvc.perform(get("/interactions/statistics")
                        .param("submissionId", "submission123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(10))
                .andExpect(jsonPath("$.commentCount").value(3));
    }

    // === Test: Get Platform Interaction Statistics ===
    @Test
    @DisplayName("✅ Should get platform-wide interaction statistics successfully")
    void testGetPlatformInteractionStatistics() throws Exception {
        Mockito.when(votesService.countAllVotes()).thenReturn(50L);
        Mockito.when(commentsService.countAllComments()).thenReturn(20L);

        mockMvc.perform(get("/interactions/public/platform/interaction-statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(50))
                .andExpect(jsonPath("$.commentCount").value(20));
    }
}
