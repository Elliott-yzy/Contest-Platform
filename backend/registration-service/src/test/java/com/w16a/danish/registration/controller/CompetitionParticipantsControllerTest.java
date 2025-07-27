package com.w16a.danish.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.service.ICompetitionParticipantsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ✅ Unit tests for CompetitionParticipantsController.
 * Focus on verifying API endpoints behavior without real database access.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompetitionParticipantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICompetitionParticipantsService participantsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Register for competition successfully")
    void testRegisterForCompetition() throws Exception {
        doNothing().when(participantsService).register(any(), any(), any());

        mockMvc.perform(post("/registrations/{competitionId}", "comp-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Cancel registration successfully")
    void testCancelRegistration() throws Exception {
        doNothing().when(participantsService).cancelRegistration(any(), any(), any());

        mockMvc.perform(delete("/registrations/{competitionId}", "comp-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List participants successfully")
    void testListParticipantsByCompetition() throws Exception {
        when(participantsService.getParticipantsByCompetitionWithSearch(any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(PageResponse.<ParticipantInfoVO>builder()
                        .page(1).size(10).total(1L).pages(1)
                        .data(List.of(new ParticipantInfoVO()))
                        .build());

        mockMvc.perform(get("/registrations/{competitionId}/participants", "comp-1")
                        .header("User-ID", "organizer-1")
                        .header("User-Role", "ORGANIZER")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Cancel participant by organizer successfully")
    void testCancelParticipantByOrganizer() throws Exception {
        doNothing().when(participantsService).cancelByOrganizer(any(), any(), any(), any());

        mockMvc.perform(delete("/registrations/{competitionId}/participants/{participantUserId}", "comp-1", "user-2")
                        .header("User-ID", "organizer-1")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Check registration status successfully")
    void testIsRegistered() throws Exception {
        when(participantsService.isRegistered(any(), any(), any())).thenReturn(true);

        mockMvc.perform(get("/registrations/{competitionId}/status", "comp-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Get competitions user registered successfully")
    void testGetMyCompetitions() throws Exception {
        when(participantsService.getMyCompetitionsWithSearch(any(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(PageResponse.<CompetitionParticipationVO>builder()
                        .page(1).size(10).total(1L).pages(1)
                        .data(List.of(new CompetitionParticipationVO()))
                        .build());

        mockMvc.perform(get("/registrations/my")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Register a team successfully")
    void testRegisterTeam() throws Exception {
        doNothing().when(participantsService).registerTeam(any(), any(), any(), any());

        mockMvc.perform(post("/registrations/teams/{competitionId}/{teamId}", "comp-1", "team-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Cancel team registration successfully")
    void testCancelTeamRegistration() throws Exception {
        doNothing().when(participantsService).cancelTeamRegistration(any(), any(), any(), any());

        mockMvc.perform(delete("/registrations/teams/{competitionId}/{teamId}", "comp-1", "team-1")
                        .header("User-ID", "user-1")
                        .header("User-Role", "PARTICIPANT"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Check if team is registered successfully")
    void testIsTeamRegistered() throws Exception {
        when(participantsService.isTeamRegistered(any(), any())).thenReturn(true);

        mockMvc.perform(get("/registrations/teams/{competitionId}/{teamId}/status", "comp-1", "team-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ List registered teams successfully")
    void testListRegisteredTeams() throws Exception {
        when(participantsService.getTeamsByCompetitionWithSearch(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(PageResponse.<TeamInfoVO>builder()
                        .page(1).size(10).total(1L).pages(1)
                        .data(List.of(new TeamInfoVO()))
                        .build());

        mockMvc.perform(get("/registrations/public/{competitionId}/teams", "comp-1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ List competitions registered by team successfully")
    void testGetCompetitionsByTeam() throws Exception {
        when(participantsService.getCompetitionsRegisteredByTeam(any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(PageResponse.<CompetitionParticipationVO>builder()
                        .page(1).size(10).total(1L).pages(1)
                        .data(List.of(new CompetitionParticipationVO()))
                        .build());

        mockMvc.perform(get("/registrations/teams/{teamId}/competitions", "team-1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Cancel team registration by organizer successfully")
    void testCancelTeamByOrganizer() throws Exception {
        doNothing().when(participantsService).cancelTeamByOrganizer(any(), any(), any(), any());

        mockMvc.perform(delete("/registrations/teams/{competitionId}/team/{teamId}/by-organizer", "comp-1", "team-1")
                        .header("User-ID", "organizer-1")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Check if team has any registration")
    void testExistsRegistrationByTeamId() throws Exception {
        when(participantsService.existsRegistrationByTeamId(any())).thenReturn(true);

        mockMvc.perform(get("/registrations/internal/exists-registration-by-team")
                        .param("teamId", "team-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Get registration statistics for competition successfully")
    void testGetRegistrationStatistics() throws Exception {
        when(participantsService.getRegistrationStatistics(any()))
                .thenReturn(new RegistrationStatisticsVO());

        mockMvc.perform(get("/registrations/public/{competitionId}/statistics", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get participant trend successfully")
    void testGetParticipantTrend() throws Exception {
        when(participantsService.getParticipantTrend(any()))
                .thenReturn(Map.of());

        mockMvc.perform(get("/registrations/public/{competitionId}/participant-trend", "comp-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get platform participant statistics successfully")
    void testGetPlatformParticipantStatistics() throws Exception {
        when(participantsService.getPlatformParticipantStatistics())
                .thenReturn(new PlatformParticipantStatisticsVO());

        mockMvc.perform(get("/registrations/public/platform/participant-statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Get platform participant trend successfully")
    void testGetPlatformParticipantTrend() throws Exception {
        when(participantsService.getPlatformParticipantTrend())
                .thenReturn(Map.of());

        mockMvc.perform(get("/registrations/public/platform/participant-trend"))
                .andExpect(status().isOk());
    }
}
