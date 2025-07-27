package com.w16a.danish.competition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.competition.domain.dto.AssignJudgesDTO;
import com.w16a.danish.competition.domain.dto.CompetitionCreateDTO;
import com.w16a.danish.competition.domain.dto.CompetitionUpdateDTO;
import com.w16a.danish.competition.domain.vo.CompetitionResponseVO;
import com.w16a.danish.competition.domain.vo.PageResponse;
import com.w16a.danish.competition.service.ICompetitionsService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICompetitionsService competitionService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ Should create competition successfully")
    void testCreateCompetition() throws Exception {
        CompetitionCreateDTO dto = new CompetitionCreateDTO();
        when(competitionService.createCompetition(any(), any(), any()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(post("/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("✅ Should get competition details successfully")
    void testGetCompetitionDetails() throws Exception {
        when(competitionService.getCompetitionById(anyString()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(get("/competitions/{id}", "test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should list competitions successfully")
    void testListCompetitions() throws Exception {
        when(competitionService.listCompetitions(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/competitions/list"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should delete competition successfully")
    void testDeleteCompetition() throws Exception {
        doNothing().when(competitionService).deleteCompetition(anyString(), anyString(), anyString());

        mockMvc.perform(delete("/competitions/delete/{id}", "test-id")
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should update competition successfully")
    void testUpdateCompetition() throws Exception {
        CompetitionUpdateDTO dto = new CompetitionUpdateDTO();
        when(competitionService.updateCompetition(anyString(), anyString(), anyString(), any()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(put("/competitions/update/{id}", "test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should upload competition media successfully")
    void testUploadCompetitionMedia() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "filename.png", "image/png", "content".getBytes());

        when(competitionService.uploadCompetitionMedia(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(multipart("/competitions/{id}/media", "test-id")
                        .file(file)
                        .param("mediaType", "IMAGE")
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should delete competition image successfully")
    void testDeleteCompetitionImage() throws Exception {
        when(competitionService.deleteCompetitionImage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(delete("/competitions/{id}/media/image", "test-id")
                        .param("imageUrl", "test-url")
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should delete intro video successfully")
    void testDeleteIntroVideo() throws Exception {
        when(competitionService.deleteIntroVideo(anyString(), anyString(), anyString()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(delete("/competitions/{id}/media/video", "test-id")
                        .header("User-ID", "userId")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should list my competitions successfully")
    void testListMyCompetitions() throws Exception {
        when(competitionService.listCompetitionsByOrganizer(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/competitions/achieve/my")
                        .header("User-ID", "userId")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should batch get competitions by IDs successfully")
    void testGetCompetitionsByIds() throws Exception {
        when(competitionService.getCompetitionsByIds(anyList()))
                .thenReturn(List.of(new CompetitionResponseVO()));

        mockMvc.perform(post("/competitions/batch/ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("id1", "id2"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should assign judges successfully")
    void testAssignJudges() throws Exception {
        AssignJudgesDTO dto = new AssignJudgesDTO();
        doNothing().when(competitionService).assignJudges(anyString(), anyString(), anyString(), any());

        mockMvc.perform(post("/competitions/{id}/assign-judges", "comp-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-ID", "userId")
                        .header("User-Role", "ORGANIZER")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should list assigned judges successfully")
    void testListAssignedJudges() throws Exception {
        when(competitionService.listAssignedJudges(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(Collections.emptyList(), 0, 1, 10, 0));

        mockMvc.perform(get("/competitions/{id}/judges", "comp-id")
                        .header("User-ID", "userId")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should remove judge successfully")
    void testRemoveJudge() throws Exception {
        doNothing().when(competitionService).removeJudge(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(delete("/competitions/{id}/judges/{judgeId}", "comp-id", "judge-id")
                        .header("User-ID", "userId")
                        .header("User-Role", "ORGANIZER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should check if user is organizer successfully")
    void testIsUserOrganizer() throws Exception {
        when(competitionService.isUserOrganizer(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/competitions/is-organizer")
                        .param("competitionId", "comp-id")
                        .param("userId", "user-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Should list all competitions successfully")
    void testListAllCompetitions() throws Exception {
        when(competitionService.listAllCompetitions()).thenReturn(List.of(new CompetitionResponseVO()));

        mockMvc.perform(get("/competitions/public/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should update competition status successfully")
    void testUpdateCompetitionStatus() throws Exception {
        when(competitionService.updateCompetitionStatus(anyString(), anyString()))
                .thenReturn(new CompetitionResponseVO());

        mockMvc.perform(put("/competitions/{id}/status", "comp-id")
                        .param("status", "ENDED"))
                .andExpect(status().isOk());
    }
}
