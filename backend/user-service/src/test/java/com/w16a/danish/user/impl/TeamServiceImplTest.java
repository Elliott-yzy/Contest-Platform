package com.w16a.danish.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.w16a.danish.user.domain.dto.TeamCreateDTO;
import com.w16a.danish.user.domain.dto.TeamUpdateDTO;
import com.w16a.danish.user.domain.po.Team;
import com.w16a.danish.user.domain.po.TeamMembers;
import com.w16a.danish.user.domain.po.Users;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.exception.BusinessException;
import com.w16a.danish.user.feign.SubmissionServiceClient;
import com.w16a.danish.user.mapper.TeamMapper;
import com.w16a.danish.user.service.ITeamMembersService;
import com.w16a.danish.user.service.IUsersService;
import com.w16a.danish.user.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeamServiceImpl}.
 * Full coverage following project standards.
 */
class TeamServiceImplTest {

    @Spy
    @InjectMocks
    private TeamServiceImpl teamService;


    @Mock
    private ITeamMembersService teamMembersService;
    @Mock
    private IUsersService usersService;
    @Mock
    private SubmissionServiceClient submissionServiceClient;
    @Mock
    private TeamMapper teamMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        java.lang.reflect.Field baseMapperField = TeamServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(teamService, teamMapper);
    }

    // === Create Team ===
    @Test
    @DisplayName("✅ Should create team successfully")
    void testCreateTeam_Success() {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setName("AI Masters");
        dto.setDescription("Team of AI enthusiasts");

        LambdaQueryChainWrapper<Team> mockQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(mockQuery).when(teamService).lambdaQuery();
        when(mockQuery.eq(any(), any())).thenReturn(mockQuery);
        when(mockQuery.exists()).thenReturn(false);

        when(usersService.getUserBriefById(anyString()))
                .thenReturn(UserBriefVO.builder().id("creatorId").name("Leader").build());

        TeamResponseVO result = teamService.createTeam("creatorId", dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
    }

    @Test
    @DisplayName("❌ Should throw when team name already exists")
    void testCreateTeam_TeamExists() {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setName("Existing Team");

        LambdaQueryChainWrapper<Team> mockQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(mockQuery).when(teamService).lambdaQuery();
        when(mockQuery.eq(any(), any())).thenReturn(mockQuery);
        when(mockQuery.exists()).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam("creatorId", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Team name already exists");
    }

    // === Remove Team Member ===
    @Test
    @DisplayName("✅ Should remove team member successfully")
    void testRemoveTeamMember_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);

        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(true);

        teamService.removeTeamMember("creatorId", "teamId", "memberId");
    }

    @Test
    @DisplayName("❌ Should throw when removing from non-existent team")
    void testRemoveTeamMember_TeamNotFound() {
        when(teamService.getById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> teamService.removeTeamMember("creatorId", "teamId", "memberId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Team not found");
    }

    // === Delete Team ===
    @Test
    @DisplayName("✅ Should delete team successfully")
    void testDeleteTeam_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(submissionServiceClient.existsByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));
        when(submissionServiceClient.existsRegistrationByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));

        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);
        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(true);

        when(teamService.removeById(anyString())).thenReturn(true);

        teamService.deleteTeam("creatorId", "Participant", "teamId");
    }

    @Test
    @DisplayName("❌ Should throw when non-creator/non-admin tries to delete")
    void testDeleteTeam_NoPermission() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("otherUser"));

        assertThatThrownBy(() -> teamService.deleteTeam("userId", "Participant", "teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only the team creator or an ADMIN");
    }

    // === Join Team ===
    @Test
    @DisplayName("✅ Should join team successfully")
    void testJoinTeam_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team());

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        when(teamMembersService.save(any())).thenReturn(true);

        teamService.joinTeam("teamId", "userId");
    }

    // === Leave Team ===
    @Test
    @DisplayName("✅ Should leave team successfully")
    void testLeaveTeam_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);

        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(true);

        teamService.leaveTeam("teamId", "userId");
    }

    // === Get Team Response ===
    @Test
    @DisplayName("✅ Should get team response by ID successfully")
    void testGetTeamResponseById_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team());

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(new TeamMembers().setUserId("uid")));

        when(usersService.getUsersByIds(anyList(), any()))
                .thenReturn(List.of(UserBriefVO.builder().id("uid").name("Member").build()));

        TeamResponseVO result = teamService.getTeamResponseById("teamId");

        assertThat(result).isNotNull();
        assertThat(result.getMembers()).isNotEmpty();
    }

    // === Get All Teams ===
    @Test
    @DisplayName("✅ Should get all teams paginated successfully")
    void testGetAllTeams_Success() {
        // mock teamService.lambdaQuery()
        LambdaQueryChainWrapper<Team> teamQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(teamQuery).when(teamService).lambdaQuery();
        when(teamQuery.list()).thenReturn(List.of(new Team().setId("tid").setName("TeamX").setCreatedBy("uid")));

        // mock teamMembersService.lambdaQuery()
        LambdaQueryChainWrapper<TeamMembers> memberQuery = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(memberQuery);
        when(memberQuery.in(any(), anyCollection())).thenReturn(memberQuery);
        when(memberQuery.list()).thenReturn(List.of(
                new TeamMembers().setTeamId("tid").setUserId("uid")
        ));

        // mock usersService.getUsersByIds()
        when(usersService.getUsersByIds(anyList(), any()))
                .thenReturn(List.of(UserBriefVO.builder().id("uid").name("Leader").build()));

        // act
        PageResponse<TeamSummaryVO> page = teamService.getAllTeams(1, 10, "createdAt", "asc", "");

        // assert
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotEmpty();
        assertThat(page.getData().get(0).getName()).isEqualTo("TeamX");
    }

    @Test
    @DisplayName("❌ Should throw when trying to remove self as creator")
    void testRemoveTeamMember_RemoveSelf() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        assertThatThrownBy(() -> teamService.removeTeamMember("creatorId", "teamId", "creatorId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot remove yourself");
    }

    @Test
    @DisplayName("❌ Should throw when removing non-existing member")
    void testRemoveTeamMember_NotExist() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        assertThatThrownBy(() -> teamService.removeTeamMember("creatorId", "teamId", "memberId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not part of this team");
    }

    @Test
    @DisplayName("❌ Should throw when failing to remove member")
    void testRemoveTeamMember_RemoveFail() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);

        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(false);

        assertThatThrownBy(() -> teamService.removeTeamMember("creatorId", "teamId", "memberId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to remove");
    }

    @Test
    @DisplayName("❌ Should throw when team has submission")
    void testDeleteTeam_HasSubmission() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(submissionServiceClient.existsByTeamId(anyString())).thenReturn(ResponseEntity.ok(true));

        assertThatThrownBy(() -> teamService.deleteTeam("creatorId", "Participant", "teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("submitted work");
    }

    @Test
    @DisplayName("❌ Should throw when team has registration")
    void testDeleteTeam_HasRegistration() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(submissionServiceClient.existsByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));
        when(submissionServiceClient.existsRegistrationByTeamId(anyString())).thenReturn(ResponseEntity.ok(true));

        assertThatThrownBy(() -> teamService.deleteTeam("creatorId", "Participant", "teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("registered for competitions");
    }

    @Test
    @DisplayName("❌ Should throw when team member removal fails during delete")
    void testDeleteTeam_RemoveMembersFail() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(submissionServiceClient.existsByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));
        when(submissionServiceClient.existsRegistrationByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));

        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);
        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam("creatorId", "Participant", "teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to remove team members");
    }

    @Test
    @DisplayName("❌ Should throw when team deletion itself fails")
    void testDeleteTeam_DeleteTeamFail() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(submissionServiceClient.existsByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));
        when(submissionServiceClient.existsRegistrationByTeamId(anyString())).thenReturn(ResponseEntity.ok(false));

        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);
        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(true);

        when(teamService.removeById(anyString())).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam("creatorId", "Participant", "teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to delete the team");
    }

    @Test
    @DisplayName("❌ Should throw when already joined team")
    void testJoinTeam_AlreadyJoined() {
        when(teamService.getById(anyString())).thenReturn(new Team());

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        assertThatThrownBy(() -> teamService.joinTeam("teamId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already in this team");
    }

    @Test
    @DisplayName("❌ Should throw when leader tries to leave team")
    void testLeaveTeam_LeaderLeave() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("userId"));

        assertThatThrownBy(() -> teamService.leaveTeam("teamId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("creator cannot leave");
    }

    @Test
    @DisplayName("❌ Should throw when user not in team")
    void testLeaveTeam_NotMember() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        assertThatThrownBy(() -> teamService.leaveTeam("teamId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    @DisplayName("❌ Should throw when leave team fails")
    void testLeaveTeam_RemoveFail() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        LambdaUpdateChainWrapper<TeamMembers> update = mock(LambdaUpdateChainWrapper.class);

        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        when(teamMembersService.lambdaUpdate()).thenReturn(update);
        when(update.eq(any(), any())).thenReturn(update);
        when(update.remove()).thenReturn(false);

        assertThatThrownBy(() -> teamService.leaveTeam("teamId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to leave");
    }

    @Test
    @DisplayName("❌ Should throw when fetching team members info fails")
    void testGetTeamResponseById_MembersFail() {
        when(teamService.getById(anyString())).thenReturn(new Team());

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(new TeamMembers().setUserId("uid")));

        when(usersService.getUsersByIds(anyList(), any())).thenReturn(null);

        assertThatThrownBy(() -> teamService.getTeamResponseById("teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fetch team member info");
    }

    @Test
    @DisplayName("❌ Should throw when team creator not found")
    void testGetTeamCreator_CreatorNotFound() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(usersService.getById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> teamService.getTeamCreator("teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("creator not found");
    }

    @Test
    @DisplayName("❌ Should throw when fetching team members returns null")
    void testGetTeamMembers_FetchFail() {
        when(teamService.getById(anyString())).thenReturn(new Team());

        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(new TeamMembers().setUserId("uid")));

        when(usersService.getUsersByIds(anyList(), any())).thenReturn(null);

        assertThatThrownBy(() -> teamService.getTeamMembers("teamId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fetch team member info");
    }

    // === Update Team ===
    @Test
    @DisplayName("✅ Should update team successfully")
    void testUpdateTeam_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team().setId("teamId").setCreatedBy("creatorId").setName("Old Name"));

        LambdaQueryChainWrapper<Team> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(teamService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.ne(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        when(teamService.updateById(any(Team.class))).thenReturn(true);

        TeamUpdateDTO dto = new TeamUpdateDTO();
        dto.setName("New Name");
        dto.setDescription("Updated description");

        teamService.updateTeam("creatorId", "teamId", dto);
    }

    @Test
    @DisplayName("❌ Should throw when updating non-existent team")
    void testUpdateTeam_TeamNotFound() {
        when(teamService.getById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> teamService.updateTeam("creatorId", "teamId", new TeamUpdateDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Team not found");
    }

    @Test
    @DisplayName("❌ Should throw when updating by non-creator")
    void testUpdateTeam_NotCreator() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("otherUser"));

        assertThatThrownBy(() -> teamService.updateTeam("userId", "teamId", new TeamUpdateDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only the team creator can update");
    }

    @Test
    @DisplayName("❌ Should throw when new team name already exists")
    void testUpdateTeam_NameExists() {
        when(teamService.getById(anyString())).thenReturn(new Team().setId("teamId").setCreatedBy("creatorId").setName("Old Name"));

        LambdaQueryChainWrapper<Team> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(teamService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.ne(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        TeamUpdateDTO dto = new TeamUpdateDTO();
        dto.setName("Existing Name");

        assertThatThrownBy(() -> teamService.updateTeam("creatorId", "teamId", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Team name already exists");
    }

    @Test
    @DisplayName("❌ Should throw when updating team info fails")
    void testUpdateTeam_UpdateFailed() {
        when(teamService.getById(anyString())).thenReturn(new Team().setId("teamId").setCreatedBy("creatorId").setName("Old Name"));

        LambdaQueryChainWrapper<Team> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(teamService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.ne(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        when(teamService.updateById(any(Team.class))).thenReturn(false);

        TeamUpdateDTO dto = new TeamUpdateDTO();
        dto.setName("New Name");

        assertThatThrownBy(() -> teamService.updateTeam("creatorId", "teamId", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to update team info");
    }

    // === Get Team Creator ===
    @Test
    @DisplayName("✅ Should get team creator info successfully")
    void testGetTeamCreator_Success() {
        when(teamService.getById(anyString())).thenReturn(new Team().setCreatedBy("creatorId"));
        when(usersService.getById(anyString()))
                .thenReturn(new Users().setId("creatorId").setName("Leader"));

        UserBriefVO result = teamService.getTeamCreator("teamId");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("creatorId");
    }

    // === Get Team Briefs by IDs ===
    @Test
    @DisplayName("✅ Should get team brief info by IDs successfully")
    void testGetTeamBriefByIds_Success() {
        LambdaQueryChainWrapper<Team> teamQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(teamQuery).when(teamService).lambdaQuery();
        when(teamQuery.in(any(), anyCollection())).thenReturn(teamQuery);
        when(teamQuery.list()).thenReturn(List.of(
                new Team().setId("tid").setName("TeamA").setDescription("DescA")
        ));

        List<String> ids = List.of("tid");
        List<TeamInfoVO> result = teamService.getTeamBriefByIds(ids);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getTeamId()).isEqualTo("tid");
    }

    // === isUserInTeam ===
    @Test
    @DisplayName("✅ Should return true when user is in team")
    void testIsUserInTeam_True() {
        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(true);

        boolean result = teamService.isUserInTeam("userId", "teamId");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ Should return false when user is not in team")
    void testIsUserInTeam_False() {
        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.exists()).thenReturn(false);

        boolean result = teamService.isUserInTeam("userId", "teamId");

        assertThat(result).isFalse();
    }

    // === getAllJoinedTeamIdsByUser ===
    @Test
    @DisplayName("✅ Should get all joined team IDs by user successfully")
    void testGetAllJoinedTeamIdsByUser_Success() {
        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(
                new TeamMembers().setTeamId("team1"),
                new TeamMembers().setTeamId("team2")
        ));

        List<String> result = teamService.getAllJoinedTeamIdsByUser("userId");

        assertThat(result).containsExactlyInAnyOrder("team1", "team2");
    }

    @Test
    @DisplayName("✅ Should return empty list when user joined no teams")
    void testGetAllJoinedTeamIdsByUser_Empty() {
        LambdaQueryChainWrapper<TeamMembers> query = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<String> result = teamService.getAllJoinedTeamIdsByUser("userId");

        assertThat(result).isEmpty();
    }

    // === getTeamsJoinedBy - No joined teams case ===
    @Test
    @DisplayName("✅ Should return empty when user joined no teams")
    void testGetTeamsJoinedBy_NoTeams() {
        LambdaQueryChainWrapper<TeamMembers> teamMembersQuery = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(teamMembersQuery);
        when(teamMembersQuery.eq(any(), any())).thenReturn(teamMembersQuery);
        when(teamMembersQuery.list()).thenReturn(List.of());

        PageResponse<TeamSummaryVO> result = teamService.getTeamsJoinedBy("userId", 1, 10, "createdAt", "asc", "");

        assertThat(result.getData()).isEmpty();
    }

    // === getTeamsCreatedBy - No created teams case ===
    @Test
    @DisplayName("✅ Should return empty when user created no teams")
    void testGetTeamsCreatedBy_NoTeams() {
        LambdaQueryChainWrapper<Team> teamQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(teamQuery).when(teamService).lambdaQuery();
        when(teamQuery.eq(any(), any())).thenReturn(teamQuery);
        when(teamQuery.list()).thenReturn(List.of());

        PageResponse<TeamSummaryVO> result = teamService.getTeamsCreatedBy("userId", 1, 10, "createdAt", "asc", "");

        assertThat(result.getData()).isEmpty();
    }

    // === getAllTeams - keyword filter ===
    @Test
    @DisplayName("✅ Should get all teams with keyword filter")
    void testGetAllTeams_WithKeyword() {
        LambdaQueryChainWrapper<Team> teamQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(teamQuery).when(teamService).lambdaQuery();
        when(teamQuery.list()).thenReturn(List.of(new Team().setId("teamId").setName("FilteredTeam").setCreatedBy("userId")));

        LambdaQueryChainWrapper<TeamMembers> memberQuery = mock(LambdaQueryChainWrapper.class);
        when(teamMembersService.lambdaQuery()).thenReturn(memberQuery);
        when(memberQuery.in(any(), anyCollection())).thenReturn(memberQuery);
        when(memberQuery.list()).thenReturn(List.of(new TeamMembers().setTeamId("teamId").setUserId("userId")));

        when(usersService.getUsersByIds(anyList(), any()))
                .thenReturn(List.of(UserBriefVO.builder().id("userId").name("FilteredUser").build()));

        PageResponse<TeamSummaryVO> result = teamService.getAllTeams(1, 10, "createdAt", "asc", "Filtered");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotEmpty();
    }

}
