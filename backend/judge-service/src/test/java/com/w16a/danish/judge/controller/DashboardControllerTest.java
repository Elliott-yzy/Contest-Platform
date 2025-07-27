package com.w16a.danish.judge.controller;

import com.w16a.danish.judge.domain.vo.CompetitionDashboardVO;
import com.w16a.danish.judge.domain.vo.PlatformDashboardVO;
import com.w16a.danish.judge.service.IDashboardService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link DashboardController}.
 * Covers API endpoints for public competition statistics and platform dashboard overview.
 * Author: Eddy Zhang
 * Date: 2025/04/27
 */
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Public: Get competition statistics overview successfully")
    void testGetCompetitionStatisticsSuccess() throws Exception {
        // Arrange
        CompetitionDashboardVO mockVO = new CompetitionDashboardVO();
        when(dashboardService.getCompetitionStatistics(anyString(), anyString())).thenReturn(mockVO);

        // Act & Assert
        mockMvc.perform(get("/dashboard/public/statistics")
                        .param("competitionId", "test-competition-id")
                        .param("userId", "test-user-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("✅ Public: Get platform dashboard overview successfully")
    void testGetPlatformDashboardSuccess() throws Exception {
        // Arrange
        PlatformDashboardVO mockVO = new PlatformDashboardVO();
        when(dashboardService.getPlatformDashboard()).thenReturn(mockVO);

        // Act & Assert
        mockMvc.perform(get("/dashboard/public/platform-overview")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
