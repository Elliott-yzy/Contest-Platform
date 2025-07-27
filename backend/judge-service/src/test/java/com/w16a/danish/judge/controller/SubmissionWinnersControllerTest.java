package com.w16a.danish.judge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.ScoredSubmissionVO;
import com.w16a.danish.judge.domain.vo.WinnerInfoVO;
import com.w16a.danish.judge.service.ISubmissionWinnersService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link SubmissionWinnersController}.
 * Covers winner awarding, public winner listing, and scored submission listing.
 * Author: Eddy Zhang
 * Date: 2025/04/27
 */
@SpringBootTest
@AutoConfigureMockMvc
class SubmissionWinnersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISubmissionWinnersService winnersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Auto award winners successfully")
    void testAutoAward() throws Exception {
        // Arrange
        doNothing().when(winnersService).autoAward(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/winners/auto-award")
                        .header("User-ID", "organizer-id")
                        .header("User-Role", "ORGANIZER")
                        .param("competitionId", "comp-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auto awarding completed successfully."));
    }

    @Test
    @DisplayName("✅ Publicly list winners successfully")
    void testListPublicWinners() throws Exception {
        // Arrange
        when(winnersService.listPublicWinners(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<WinnerInfoVO>());

        // Act & Assert
        mockMvc.perform(get("/winners/public-list")
                        .param("competitionId", "comp-123")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("✅ List scored submissions successfully")
    void testListScoredSubmissions() throws Exception {
        // Arrange
        when(winnersService.listScoredSubmissions(anyString(), anyString(), anyString(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<ScoredSubmissionVO>());

        // Act & Assert
        mockMvc.perform(get("/winners/scored-list")
                        .header("User-ID", "organizer-id")
                        .header("User-Role", "ORGANIZER")
                        .param("competitionId", "comp-123")
                        .param("keyword", "AI Project")
                        .param("sortBy", "totalScore")
                        .param("order", "desc")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
