package com.w16a.danish.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w16a.danish.user.domain.dto.TeamCreateDTO;
import com.w16a.danish.user.domain.dto.TeamUpdateDTO;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.service.ITeamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
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
class TeamControllerTest {

    @TestConfiguration
    static class MockRabbitConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        public RabbitListenerContainerFactory<SimpleMessageListenerContainer> rabbitListenerContainerFactory() {
            return invocation -> mock(SimpleMessageListenerContainer.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITeamService teamService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("✅ Should create team successfully")
    void testCreateTeam() throws Exception {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setName("AI Champions");
        dto.setDescription("We love competitions!");

        when(teamService.createTeam(eq("creator-1"), any(TeamCreateDTO.class)))
                .thenReturn(new TeamResponseVO());

        mockMvc.perform(post("/teams/create")
                        .header("User-ID", "creator-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("✅ Should remove a member successfully")
    void testRemoveMember() throws Exception {
        doNothing().when(teamService).removeTeamMember("leader-1", "team-1", "member-1");

        mockMvc.perform(delete("/teams/team-1/members/member-1")
                        .header("User-ID", "leader-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Member removed"));
    }

    @Test
    @DisplayName("✅ Should delete team successfully")
    void testDeleteTeam() throws Exception {
        doNothing().when(teamService).deleteTeam("admin-1", "ADMIN", "team-1");

        mockMvc.perform(delete("/teams/team-1")
                        .header("User-ID", "admin-1")
                        .header("User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Team deleted"));
    }

    @Test
    @DisplayName("✅ Should update team successfully")
    void testUpdateTeam() throws Exception {
        TeamUpdateDTO dto = new TeamUpdateDTO();
        dto.setName("New Name");

        doNothing().when(teamService).updateTeam("leader-1", "team-1", dto);

        mockMvc.perform(put("/teams/team-1")
                        .header("User-ID", "leader-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Team updated"));
    }

    @Test
    @DisplayName("✅ Should join team successfully")
    void testJoinTeam() throws Exception {
        doNothing().when(teamService).joinTeam("team-1", "user-1");

        mockMvc.perform(post("/teams/team-1/join")
                        .header("User-ID", "user-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Joined successfully"));
    }

    @Test
    @DisplayName("✅ Should leave team successfully")
    void testLeaveTeam() throws Exception {
        doNothing().when(teamService).leaveTeam("team-1", "user-1");

        mockMvc.perform(post("/teams/team-1/leave")
                        .header("User-ID", "user-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Left the team"));
    }

    @Test
    @DisplayName("✅ Should get team detail by ID")
    void testGetTeamById() throws Exception {
        when(teamService.getTeamResponseById("team-1")).thenReturn(new TeamResponseVO());

        mockMvc.perform(get("/teams/public/team-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get created teams by user")
    void testGetCreatedTeamsByUser() throws Exception {
        when(teamService.getTeamsCreatedBy(anyString(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/public/created")
                        .param("userId", "creator-1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get my joined teams")
    void testGetMyJoinedTeams() throws Exception {
        when(teamService.getTeamsJoinedBy(anyString(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/my-joined")
                        .header("User-ID", "user-1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get all teams")
    void testGetAllTeams() throws Exception {
        when(teamService.getAllTeams(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/public/all")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get team creator")
    void testGetTeamCreator() throws Exception {
        when(teamService.getTeamCreator("team-1")).thenReturn(new UserBriefVO());

        mockMvc.perform(get("/teams/team-1/creator"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get team brief by IDs")
    void testGetTeamBriefByIds() throws Exception {
        when(teamService.getTeamBriefByIds(anyList()))
                .thenReturn(Collections.singletonList(new TeamInfoVO()));

        mockMvc.perform(post("/teams/public/brief")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("team-1"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should check if user is in team")
    void testIsUserInTeam() throws Exception {
        when(teamService.isUserInTeam("user-1", "team-1")).thenReturn(true);

        mockMvc.perform(get("/teams/public/is-member")
                        .param("userId", "user-1")
                        .param("teamId", "team-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("✅ Should get team members")
    void testGetTeamMembers() throws Exception {
        when(teamService.getTeamMembers("team-1")).thenReturn(Collections.singletonList(new UserBriefVO()));

        mockMvc.perform(get("/teams/public/team-1/members"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get joined team IDs by user")
    void testGetJoinedTeamIdsByUser() throws Exception {
        when(teamService.getAllJoinedTeamIdsByUser("user-1")).thenReturn(List.of("team-1"));

        mockMvc.perform(get("/teams/public/joined")
                        .param("userId", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("❌ Should fail to create team without User-ID header")
    void testCreateTeamWithoutUserId() throws Exception {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setName("Team Without Header");

        mockMvc.perform(post("/teams/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Should fail to remove member without User-ID header")
    void testRemoveMemberWithoutUserId() throws Exception {
        mockMvc.perform(delete("/teams/team-1/members/member-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Should fail to delete team without headers")
    void testDeleteTeamWithoutHeaders() throws Exception {
        mockMvc.perform(delete("/teams/team-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ Should get created teams with keyword filter")
    void testGetCreatedTeamsWithKeyword() throws Exception {
        when(teamService.getTeamsCreatedBy(anyString(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/public/created")
                        .param("userId", "creator-1")
                        .param("keyword", "AI")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get joined teams with keyword filter")
    void testGetJoinedTeamsWithKeyword() throws Exception {
        when(teamService.getTeamsJoinedBy(anyString(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/my-joined")
                        .header("User-ID", "user-1")
                        .param("keyword", "AI")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("✅ Should get all teams with keyword filter")
    void testGetAllTeamsWithKeyword() throws Exception {
        when(teamService.getAllTeams(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageResponse<>());

        mockMvc.perform(get("/teams/public/all")
                        .param("keyword", "competition")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }


}
