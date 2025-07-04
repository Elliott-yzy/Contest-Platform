package com.w16a.danish.registration.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.w16a.danish.registration.config.RegistrationNotifier;
import com.w16a.danish.registration.domain.po.CompetitionOrganizers;
import com.w16a.danish.registration.domain.po.CompetitionParticipants;
import com.w16a.danish.registration.domain.po.CompetitionTeams;
import com.w16a.danish.registration.domain.po.SubmissionRecords;
import com.w16a.danish.registration.domain.vo.CompetitionResponseVO;
import com.w16a.danish.registration.domain.vo.TeamInfoVO;
import com.w16a.danish.registration.domain.vo.UserBriefVO;
import com.w16a.danish.registration.enums.CompetitionStatus;
import com.w16a.danish.registration.enums.ParticipationType;
import com.w16a.danish.registration.exception.BusinessException;
import com.w16a.danish.registration.feign.CompetitionServiceClient;
import com.w16a.danish.registration.feign.UserServiceClient;
import com.w16a.danish.registration.mapper.CompetitionParticipantsMapper;
import com.w16a.danish.registration.service.ICompetitionOrganizersService;
import com.w16a.danish.registration.service.ICompetitionTeamsService;
import com.w16a.danish.registration.service.ISubmissionRecordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompetitionParticipantsServiceImplTest {

    private CompetitionParticipantsServiceImpl service;
    private CompetitionParticipantsMapper participantsMapper;
    private CompetitionServiceClient competitionServiceClient;
    private ICompetitionOrganizersService competitionOrganizersService;
    private UserServiceClient userServiceClient;
    private ISubmissionRecordsService submissionService;
    private RegistrationNotifier registrationNotifier;
    private ICompetitionTeamsService competitionTeamsService;

    // we'll reuse this stub for all calls to service.lambdaQuery()
    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<CompetitionParticipants> partQuery;

    @BeforeEach
    void setUp() throws Exception {
        // 1) mock collaborators
        competitionServiceClient      = mock(CompetitionServiceClient.class);
        competitionOrganizersService = mock(ICompetitionOrganizersService.class);
        userServiceClient            = mock(UserServiceClient.class);
        submissionService            = mock(ISubmissionRecordsService.class);
        registrationNotifier         = mock(RegistrationNotifier.class);
        competitionTeamsService      = mock(ICompetitionTeamsService.class);

        // 2) create a stubbed LambdaQueryChainWrapper for participants
        partQuery = mock(LambdaQueryChainWrapper.class);

        // 3) instantiate an anonymous subclass that ONLY overrides lambdaQuery()
        CompetitionParticipantsServiceImpl real = new CompetitionParticipantsServiceImpl(
                competitionServiceClient,
                competitionOrganizersService,
                userServiceClient,
                submissionService,
                registrationNotifier,
                competitionTeamsService
        ) {
            @Override
            public LambdaQueryChainWrapper<CompetitionParticipants> lambdaQuery() {
                return partQuery;
            }
        };

        // 4) spy it so we can stub save(), removeById(), etc.
        service = spy(real);
    }

    @Test
    @DisplayName("✅ Should register for a competition successfully")
    void testRegister_Success() {
        // stub competition-service
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.UPCOMING);
        when(competitionServiceClient.getCompetitionById("comp-1"))
                .thenReturn(ResponseEntity.ok(competition));

        // stub user-service
        UserBriefVO user = new UserBriefVO();
        user.setName("Tester");
        user.setEmail("t@example.com");
        when(userServiceClient.getUserBriefById("user-1"))
                .thenReturn(ResponseEntity.ok(user));

        // stub "no prior registration"
        when(partQuery.eq(any(), any())).thenReturn(partQuery);
        when(partQuery.exists()).thenReturn(false);

        // stub save
        doReturn(true).when(service).save(any(CompetitionParticipants.class));

        // exercise
        assertThatCode(() -> service.register(
                "comp-1", "user-1", "PARTICIPANT"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should cancel registration successfully")
    void testCancelRegistration_Success() {
        // stub find existing participant
        when(partQuery.eq(any(), any())).thenReturn(partQuery);
        when(partQuery.one()).thenReturn(new CompetitionParticipants().setId("some-id"));

        // stub submission check → no submissions
        @SuppressWarnings("unchecked")
        var subQ = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(subQ);
        when(subQ.eq(any(), any())).thenReturn(subQ);
        when(subQ.exists()).thenReturn(false);

        // stub remove
        doReturn(true).when(service).removeById("some-id");

        assertThatCode(() -> service.cancelRegistration(
                "comp-1", "user-1", "PARTICIPANT"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should check registration status successfully")
    void testIsRegistered_Success() {
        when(partQuery.eq(any(), any())).thenReturn(partQuery);
        when(partQuery.exists()).thenReturn(true);

        assertThatCode(() -> service.isRegistered(
                "comp-1", "user-1", "PARTICIPANT"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get my competitions successfully")
    void testGetMyCompetitionsWithSearch_Success() {
        // stub participants list
        when(partQuery.eq(any(), any())).thenReturn(partQuery);
        when(partQuery.list()).thenReturn(Collections.emptyList());

        // stub remote calls
        when(submissionService.getSubmissionStatus(anyString(), anyList()))
                .thenReturn(Collections.emptyMap());
        when(submissionService.getSubmissionScores(anyString(), anyList()))
                .thenReturn(Collections.emptyMap());
        when(competitionServiceClient.getCompetitionsByIds(anyList()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        assertThatCode(() -> service.getMyCompetitionsWithSearch(
                "user-1","PARTICIPANT",1,10,null,null,null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should cancel by organizer successfully")
    void testCancelByOrganizer_Success() {
        // stub organizer-ownership
        @SuppressWarnings("unchecked")
        var orgQ = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(orgQ);
        when(orgQ.eq(any(), any())).thenReturn(orgQ);
        when(orgQ.exists()).thenReturn(true);

        // stub participant lookup
        when(partQuery.eq(any(), any())).thenReturn(partQuery);
        when(partQuery.one()).thenReturn(new CompetitionParticipants().setId("pid"));

        // stub submission check → none
        @SuppressWarnings("unchecked")
        var subQ = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(subQ);
        when(subQ.eq(any(), any())).thenReturn(subQ);
        when(subQ.exists()).thenReturn(false);

        // stub removal
        doReturn(true).when(service).removeById("pid");

        // stub user lookups
        when(userServiceClient.getUserBriefById(anyString()))
                .thenReturn(ResponseEntity.ok(new UserBriefVO()));
        when(competitionServiceClient.getCompetitionById(anyString()))
                .thenReturn(ResponseEntity.ok(new CompetitionResponseVO()));

        assertThatCode(() -> service.cancelByOrganizer(
                "comp-1","user-2","org-1","ORGANIZER"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should register a team successfully")
    void testRegisterTeam_Success() {
        // 1) team-creator check
        UserBriefVO creator = new UserBriefVO(); creator.setId("user-1");
        when(userServiceClient.getTeamCreator("team-1"))
                .thenReturn(ResponseEntity.ok(creator));

        // 2) competition must be TEAM mode
        CompetitionResponseVO comp = new CompetitionResponseVO();
        comp.setStatus(CompetitionStatus.UPCOMING);
        comp.setParticipationType(ParticipationType.TEAM);
        when(competitionServiceClient.getCompetitionById("comp-1"))
                .thenReturn(ResponseEntity.ok(comp));

        // 3) no existing team registration
        @SuppressWarnings("unchecked")
        var teamQ = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamQ);
        when(teamQ.eq(any(), any())).thenReturn(teamQ);
        when(teamQ.exists()).thenReturn(false);

        // 4) save succeeds
        when(competitionTeamsService.save(any(CompetitionTeams.class)))
                .thenReturn(true);

        // 5) notify
        UserBriefVO user = new UserBriefVO();
        user.setName("Tester"); user.setEmail("t@example.com");
        when(userServiceClient.getUserBriefById("user-1"))
                .thenReturn(ResponseEntity.ok(user));

        assertThatCode(() -> service.registerTeam(
                "comp-1","team-1","user-1","PARTICIPANT"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should cancel team registration successfully")
    void testCancelTeamRegistration_Success() {
        // 1) team-creator check
        UserBriefVO creator = new UserBriefVO(); creator.setId("user-1");
        when(userServiceClient.getTeamCreator("team-1"))
                .thenReturn(ResponseEntity.ok(creator));

        // 2) find existing registration
        CompetitionTeams reg = new CompetitionTeams();
        reg.setId("r1");
        @SuppressWarnings("unchecked")
        var teamQ = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamQ);
        when(teamQ.eq(any(), any())).thenReturn(teamQ);
        when(teamQ.one()).thenReturn(reg);

        // 3) no team submissions
        @SuppressWarnings("unchecked")
        var subQ = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(subQ);
        when(subQ.eq(any(), any())).thenReturn(subQ);
        when(subQ.exists()).thenReturn(false);

        // 4) remove
        when(competitionTeamsService.removeById("r1")).thenReturn(true);

        assertThatCode(() -> service.cancelTeamRegistration(
                "comp-1","team-1","user-1","PARTICIPANT"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get platform statistics successfully")
    void testGetPlatformParticipantStatistics_Success() {
        // stub total individual
        when(partQuery.isNotNull(any())).thenReturn(partQuery);
        when(partQuery.count()).thenReturn(42L);

        // stub team total
        when(competitionTeamsService.countTeamParticipants()).thenReturn(7);

        assertThatCode(() -> service.getPlatformParticipantStatistics())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get platform trend successfully")
    void testGetPlatformParticipantTrend_Success() {
        // individual side
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionParticipants> indivQ = mock(LambdaQueryChainWrapper.class);
        when(service.lambdaQuery()).thenReturn(indivQ);
        // explicitly match the SFunction[] var‐args overload
        when(indivQ.select(
                (SFunction<CompetitionParticipants, ?>[]) any(SFunction[].class)
        )).thenReturn(indivQ);
        when(indivQ.list()).thenReturn(Collections.emptyList());

        // team side
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionTeams> teamQ = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamQ);
        // same trick for CompetitionTeams
        when(teamQ.select(
                (SFunction<CompetitionTeams, ?>[]) any(SFunction[].class)
        )).thenReturn(teamQ);
        when(teamQ.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> service.getPlatformParticipantTrend())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should list competitions registered by team successfully")
    void testGetCompetitionsRegisteredByTeam_Success() {
        // 1) Stub competitionTeamsService.lambdaQuery().eq(...).list()
        CompetitionTeams reg = new CompetitionTeams()
                .setCompetitionId("comp-1")
                .setJoinedAt(LocalDateTime.now());
        @SuppressWarnings("unchecked")
        var teamQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamQuery);
        // MATCH the SFunction overload, not any(Object,Object)
        when(teamQuery.eq(any(SFunction.class), eq("team-1"))).thenReturn(teamQuery);
        when(teamQuery.list()).thenReturn(List.of(reg));

        // 2) Stub CompetitionServiceClient.getCompetitionsByIds(...)
        CompetitionResponseVO comp = new CompetitionResponseVO();
        comp.setId("comp-1");
        comp.setName("CodeFest");
        comp.setCategory("Coding");
        comp.setStatus(CompetitionStatus.ONGOING);
        comp.setStartDate(LocalDateTime.now().minusDays(1));
        comp.setEndDate(LocalDateTime.now().plusDays(1));
        comp.setIsPublic(true);
        when(competitionServiceClient.getCompetitionsByIds(List.of("comp-1")))
                .thenReturn(ResponseEntity.ok(List.of(comp)));

        // 3) Stub submissionService maps
        when(submissionService.getSubmissionStatusByTeam(
                List.of("team-1"), List.of("comp-1")))
                .thenReturn(Map.of("comp-1:team-1", true));
        when(submissionService.getSubmissionScoresByTeam(
                List.of("team-1"), List.of("comp-1")))
                .thenReturn(Map.of("comp-1:team-1", BigDecimal.valueOf(95)));

        // Act & Assert
        assertThatCode(() -> service.getCompetitionsRegisteredByTeam(
                "team-1", 1, 10, null, "competitionName", "asc"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should list participants by competition successfully")
    void testGetParticipantsByCompetitionWithSearch_Success() {
        // 1) Organizer must be owner
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionOrganizers> orgQ = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(orgQ);
        when(orgQ.eq(any(SFunction.class), eq("comp-1"))).thenReturn(orgQ);
        when(orgQ.eq(any(SFunction.class), eq("org-1"))).thenReturn(orgQ);
        when(orgQ.exists()).thenReturn(true);

        // 2) Stub participants query
        CompetitionParticipants cp = new CompetitionParticipants()
                .setUserId("user-1")
                .setCreatedAt(LocalDateTime.now());
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionParticipants> partQ = mock(LambdaQueryChainWrapper.class);
        when(service.lambdaQuery()).thenReturn(partQ);
        // match the SFunction eq overload
        when(partQ.eq(any(SFunction.class), eq("comp-1"))).thenReturn(partQ);
        when(partQ.list()).thenReturn(List.of(cp));

        // 3) Stub user-service lookup
        UserBriefVO ub = new UserBriefVO();
        ub.setId("user-1");
        ub.setName("Alice");
        ub.setEmail("alice@example.com");
        when(userServiceClient.getUsersByIds(List.of("user-1"), "PARTICIPANT"))
                .thenReturn(ResponseEntity.ok(List.of(ub)));

        // Act & Assert: should not throw
        assertThatCode(() -> service.getParticipantsByCompetitionWithSearch(
                "comp-1",
                "org-1",
                "ORGANIZER",
                1, 10,
                null,
                "registeredAt",
                "desc"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should list teams by competition successfully")
    void testGetTeamsByCompetitionWithSearch_Success() {
        // 1) Stub competitionTeamsService.lambdaQuery().eq(...).list()
        CompetitionTeams teamRec = new CompetitionTeams()
                .setTeamId("team-1")
                .setJoinedAt(LocalDateTime.now());

        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionTeams> teamQ = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamQ);
        // match any SFunction for the field selector and the exact competitionId
        when(teamQ.eq(any(SFunction.class), eq("comp-1"))).thenReturn(teamQ);
        when(teamQ.list()).thenReturn(List.of(teamRec));

        // 2) Stub userServiceClient.getTeamBriefByIds(...)
        TeamInfoVO tvo = new TeamInfoVO();
        tvo.setTeamId("team-1");
        tvo.setTeamName("Best Team");
        tvo.setDescription("We rock");
        when(userServiceClient.getTeamBriefByIds(eq(List.of("team-1"))))
                .thenReturn(ResponseEntity.ok(List.of(tvo)));

        // Act & Assert: should not throw
        assertThatCode(() -> service.getTeamsByCompetitionWithSearch(
                "comp-1",  // competitionId
                1,         // page
                10,        // size
                null,      // keyword
                "teamName",// sortBy
                "asc"      // order
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get participant trend successfully")
    void testGetParticipantTrend_Success() {
        // 1) Stub competition lookup
        CompetitionResponseVO compVo = new CompetitionResponseVO();
        doReturn(ResponseEntity.ok(compVo))
                .when(competitionServiceClient).getCompetitionById("comp-1");

        // 2) Stub individual registrations query
        CompetitionParticipants ip1 = new CompetitionParticipants()
                .setCreatedAt(LocalDateTime.now().minusDays(2));
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionParticipants> indivQ =
                mock(LambdaQueryChainWrapper.class);
        // ensure service.lambdaQuery() returns our mock wrapper
        doReturn(indivQ).when(service).lambdaQuery();
        // stub the .eq(...) and .list() chain
        when(indivQ.eq(any(SFunction.class), eq("comp-1"))).thenReturn(indivQ);
        when(indivQ.list()).thenReturn(List.of(ip1));

        // 3) Stub team registrations query
        CompetitionTeams tp1 = new CompetitionTeams()
                .setJoinedAt(LocalDateTime.now().minusDays(1));
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionTeams> teamQ =
                mock(LambdaQueryChainWrapper.class);
        doReturn(teamQ).when(competitionTeamsService).lambdaQuery();
        when(teamQ.eq(any(SFunction.class), eq("comp-1"))).thenReturn(teamQ);
        when(teamQ.list()).thenReturn(List.of(tp1));

        // Act & Assert
        assertThatCode(() -> service.getParticipantTrend("comp-1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get registration statistics successfully")
    void testGetRegistrationStatistics_Success() {
        // 1) Stub competition lookup
        CompetitionResponseVO comp = new CompetitionResponseVO();
        when(competitionServiceClient.getCompetitionById(eq("comp-1")))
                .thenReturn(ResponseEntity.ok(comp));

        // 2) Stub service.lambdaQuery() for individual participants
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionParticipants> partQ =
                mock(LambdaQueryChainWrapper.class);
        when(service.lambdaQuery()).thenReturn(partQ);
        when(partQ.eq(any(), any())).thenReturn(partQ);
        when(partQ.count()).thenReturn(5L);

        // 3) Stub competitionTeamsService.lambdaQuery() for team participants
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionTeams> teamPartQ =
                mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(teamPartQ);
        when(teamPartQ.eq(any(), any())).thenReturn(teamPartQ);
        when(teamPartQ.count()).thenReturn(3L);

        // Act & Assert
        assertThatCode(() -> service.getRegistrationStatistics("comp-1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should check team‐registration flags successfully")
    void testIsTeamRegisteredAndExistsByTeamId_Success() {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<CompetitionTeams> tq = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(tq);
        when(tq.eq(any(), any())).thenReturn(tq);

        // exists() for isTeamRegistered
        when(tq.exists()).thenReturn(true);
        assertThatCode(() -> service.isTeamRegistered("comp-1","team-1"))
                .doesNotThrowAnyException();

        // same call for existsRegistrationByTeamId
        when(tq.exists()).thenReturn(false);
        assertThatCode(() -> service.existsRegistrationByTeamId("team-1"))
                .doesNotThrowAnyException();
    }

    //––– cancelTeamByOrganizer negative-branches –––

    @Test
    @DisplayName("❌ cancelTeamByOrganizer throws FORBIDDEN if wrong role")
    void testCancelTeamByOrganizer_WrongRole() {
        assertThatThrownBy(() ->
                service.cancelTeamByOrganizer("comp-1", "team-1", "user-1", "PARTICIPANT")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only ORGANIZER or ADMIN");
    }

    @Test
    @DisplayName("❌ cancelTeamByOrganizer throws FORBIDDEN if not organizer nor admin")
    void testCancelTeamByOrganizer_NotOwner() {
        @SuppressWarnings("unchecked")
        var orgQ = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(orgQ);
        when(orgQ.eq(any(), any())).thenReturn(orgQ);
        when(orgQ.exists()).thenReturn(false);

        assertThatThrownBy(() ->
                service.cancelTeamByOrganizer("comp-1","team-1","admin-1","ORGANIZER")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("❌ cancelTeamByOrganizer throws NOT_FOUND if no registration record")
    void testCancelTeamByOrganizer_NotFound() {
        @SuppressWarnings("unchecked")
        var orgQ = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(orgQ);
        when(orgQ.eq(any(), any())).thenReturn(orgQ);
        when(orgQ.exists()).thenReturn(true);

        @SuppressWarnings("unchecked")
        var findQ = mock(LambdaQueryChainWrapper.class);
        when(competitionTeamsService.lambdaQuery()).thenReturn(findQ);
        when(findQ.eq(any(), any())).thenReturn(findQ);
        when(findQ.one()).thenReturn(null);

        assertThatThrownBy(() ->
                service.cancelTeamByOrganizer("comp-1","team-1","admin-1","ORGANIZER")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("not registered");
    }

//––– getMyCompetitionsWithSearch –––

    @Test
    @DisplayName("✅ getMyCompetitionsWithSearch returns empty page when no registrations")
    void testGetMyCompetitionsWithSearch_Empty() {
        @SuppressWarnings("unchecked")
        var partQ = mock(LambdaQueryChainWrapper.class);
        when(service.lambdaQuery()).thenReturn(partQ);
        when(partQ.eq(any(), any())).thenReturn(partQ);
        when(partQ.list()).thenReturn(Collections.emptyList());

        var resp = service.getMyCompetitionsWithSearch(
                "user-1", "PARTICIPANT",
                1, 10,
                null, null, null);

        assertThat(resp.getTotal()).isZero();
    }

    @Test
    @DisplayName("✅ getMyCompetitionsWithSearch filters and sorts results")
    void testGetMyCompetitionsWithSearch_FilterAndSort() {
        // 1) Stub two competition‐participant records
        CompetitionParticipants p1 = new CompetitionParticipants()
                .setCompetitionId("A")
                .setCreatedAt(LocalDateTime.now().minusDays(2));
        CompetitionParticipants p2 = new CompetitionParticipants()
                .setCompetitionId("B")
                .setCreatedAt(LocalDateTime.now().minusDays(1));
        @SuppressWarnings("unchecked")
        var partQ = mock(LambdaQueryChainWrapper.class);
        when(service.lambdaQuery()).thenReturn(partQ);
        when(partQ.eq(any(), any())).thenReturn(partQ);
        when(partQ.list()).thenReturn(List.of(p1, p2));

        // 2) Stub the remote CompetitionServiceClient call
        CompetitionResponseVO voA = new CompetitionResponseVO();
        voA.setId("A");
        voA.setName("Alpha");
        voA.setCategory("X");
        voA.setStatus(CompetitionStatus.UPCOMING);
        voA.setStartDate(LocalDateTime.now().minusDays(5));
        voA.setEndDate(LocalDateTime.now().minusDays(3));

        CompetitionResponseVO voB = new CompetitionResponseVO();
        voB.setId("B");
        voB.setName("Beta");
        voB.setCategory("Y");
        voB.setStatus(CompetitionStatus.UPCOMING);
        voB.setStartDate(LocalDateTime.now().minusDays(4));
        voB.setEndDate(LocalDateTime.now().minusDays(2));

        when(competitionServiceClient.getCompetitionsByIds(List.of("A", "B")))
                .thenReturn(ResponseEntity.ok(List.of(voA, voB)));

        // 3) Stub empty submission maps
        when(submissionService.getSubmissionStatus("user-1", List.of("A", "B")))
                .thenReturn(Map.of());
        when(submissionService.getSubmissionScores("user-1", List.of("A", "B")))
                .thenReturn(Map.of());

        // Act: filter by keyword "be" (matches "Beta") and sort descending by competitionName
        var resp = service.getMyCompetitionsWithSearch(
                "user-1", "PARTICIPANT",
                1, 10,
                "be",          // keyword
                "competitionName",
                "desc");

        // Assert: only "Beta" remains
        assertThat(resp.getData().get(0).getCompetitionName()).isEqualTo("Beta");
    }

//––– register / isRegistered –––

    @Test
    @DisplayName("❌ register throws if not PARTICIPANT")
    void testRegister_WrongRole() {
        assertThatThrownBy(() ->
                service.register("c","u","ADMIN")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PARTICIPANT");
    }

    @Test
    @DisplayName("❌ isRegistered throws if not PARTICIPANT")
    void testIsRegistered_WrongRole() {
        assertThatThrownBy(() ->
                service.isRegistered("c","u","ORGANIZER")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PARTICIPANT");
    }

}
