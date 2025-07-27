package com.w16a.danish.registration.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.w16a.danish.registration.config.SubmissionNotifier;
import com.w16a.danish.registration.domain.dto.SubmissionReviewDTO;
import com.w16a.danish.registration.domain.po.CompetitionOrganizers;
import com.w16a.danish.registration.domain.po.CompetitionParticipants;
import com.w16a.danish.registration.domain.po.SubmissionRecords;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.enums.CompetitionStatus;
import com.w16a.danish.registration.exception.BusinessException;
import com.w16a.danish.registration.feign.CompetitionServiceClient;
import com.w16a.danish.registration.feign.FileServiceClient;
import com.w16a.danish.registration.feign.UserServiceClient;
import com.w16a.danish.registration.mapper.SubmissionRecordsMapper;
import com.w16a.danish.registration.service.ICompetitionOrganizersService;
import com.w16a.danish.registration.service.ICompetitionParticipantsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubmissionRecordsServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionRecordsServiceImpl submissionService;

    @Mock private SubmissionRecordsMapper submissionRecordsMapper;
    @Mock private CompetitionServiceClient competitionServiceClient;
    @Mock private FileServiceClient fileServiceClient;
    @Mock private SubmissionNotifier submissionNotifier;
    @Mock private UserServiceClient userServiceClient;
    @Mock private ICompetitionParticipantsService competitionParticipantsService;
    @Mock private ICompetitionOrganizersService competitionOrganizersService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        Field baseMapperField = SubmissionRecordsServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(submissionService, submissionRecordsMapper);

        Field participantsField = SubmissionRecordsServiceImpl.class.getDeclaredField("competitionParticipantsService");
        participantsField.setAccessible(true);
        participantsField.set(submissionService, competitionParticipantsService);

        Field organizersField = SubmissionRecordsServiceImpl.class.getDeclaredField("competitionOrganizersService");
        organizersField.setAccessible(true);
        organizersField.set(submissionService, competitionOrganizersService);

        // Mock participants lambda query
        LambdaQueryChainWrapper<CompetitionParticipants> participantQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionParticipantsService.lambdaQuery()).thenReturn(participantQuery);
        when(participantQuery.eq(any(), any())).thenReturn(participantQuery);
        when(participantQuery.exists()).thenReturn(true);

        // Mock organizers lambda query
        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(false);

        // Mock submission lambda query
        LambdaQueryChainWrapper<SubmissionRecords> submissionQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(submissionQuery).when(submissionService).lambdaQuery();
        when(submissionQuery.eq(any(), any())).thenReturn(submissionQuery);
        when(submissionQuery.in(any(), anyCollection())).thenReturn(submissionQuery);
        when(submissionQuery.isNotNull(any())).thenReturn(submissionQuery);
        when(submissionQuery.list()).thenReturn(Collections.emptyList());
        when(submissionQuery.one()).thenReturn(null);
    }

    @Test
    @DisplayName("✅ Should submit work successfully when user is PARTICIPANT and competition is ongoing")
    void testSubmitWork_Success() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok("http://mockurl.com/file"));

        UserBriefVO user = new UserBriefVO();
        user.setName("Test User");
        user.setEmail("test@example.com");
        when(userServiceClient.getUserBriefById(userId)).thenReturn(ResponseEntity.ok(user));

        when(submissionRecordsMapper.insert(any(SubmissionRecords.class))).thenReturn(1);

        assertThatCode(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Submission Title", "Submission Description", file))
                .doesNotThrowAnyException();

    }

    @Test
    @DisplayName("❌ Should throw BusinessException if user role is not PARTICIPANT")
    void testSubmitWork_ForbiddenIfNotParticipant() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                "user-1", "ORGANIZER", "comp-1", "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PARTICIPANT role can submit work");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when competition is ended")
    void testSubmitWork_CompetitionEnded() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().minusDays(1)); // 已经结束
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already ended");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when file upload fails")
    void testSubmitWork_FileUploadFails() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok(null));

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("File upload failed");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when competition not found")
    void testSubmitWork_CompetitionNotFound() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(null));

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Competition not found");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when user info not found (actually fails to save)")
    void testSubmitWork_UserNotFound() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok("http://mockurl.com/file"));
        when(userServiceClient.getUserBriefById(userId)).thenReturn(ResponseEntity.ok(null));

        when(submissionRecordsMapper.insert(any(SubmissionRecords.class))).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to save submission");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when submission save fails")
    void testSubmitWork_SaveFails() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok("http://mockurl.com/file"));

        UserBriefVO user = new UserBriefVO();
        user.setName("Test User");
        user.setEmail("test@example.com");
        when(userServiceClient.getUserBriefById(userId)).thenReturn(ResponseEntity.ok(user));

        when(submissionRecordsMapper.insert(any(SubmissionRecords.class))).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to save submission");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when submission already exists")
    void testSubmitWork_SubmissionAlreadyExists() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));
        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok("http://mockurl.com/file"));

        SubmissionRecords existingRecord = new SubmissionRecords();
        LambdaQueryChainWrapper<SubmissionRecords> submissionQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(submissionQuery).when(submissionService).lambdaQuery();
        when(submissionQuery.eq(any(), any())).thenReturn(submissionQuery);
        when(submissionQuery.in(any(), anyCollection())).thenReturn(submissionQuery);
        when(submissionQuery.one()).thenReturn(existingRecord);

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to update submission");
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when file upload returns blank URL")
    void testSubmitWork_FileUploadReturnsBlank() {
        // Arrange: Prepare mock data
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filename.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        // Mock competition information: ongoing and not ended
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId))
                .thenReturn(ResponseEntity.ok(competition));

        // Mock file upload: returns blank space (invalid)
        when(fileServiceClient.uploadSubmission(file))
                .thenReturn(ResponseEntity.ok(" "));

        // Act & Assert: Expect BusinessException with general save submission error
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to save submission"); // Correct expected message
    }

    @Test
    @DisplayName("❌ Should throw BusinessException when user brief fetch fails with exception")
    void testSubmitWork_UserBriefFetchException() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "dummy content".getBytes());

        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId)).thenReturn(ResponseEntity.ok(competition));

        when(fileServiceClient.uploadSubmission(file)).thenReturn(ResponseEntity.ok("http://mockurl.com/file"));
        when(userServiceClient.getUserBriefById(userId)).thenThrow(new RuntimeException("Feign Client error"));

        // Act & Assert
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Failed to save submission");
    }

    @Test
    @DisplayName("❌ Should throw RuntimeException when file upload throws exception")
    void testSubmitWork_FileUploadException() {
        // Arrange: Prepare mock data
        String userId = "user-1";
        String competitionId = "comp-1";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filename.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        // Mock competition info: competition is ongoing
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId))
                .thenReturn(ResponseEntity.ok(competition));

        // Mock file upload throwing a RuntimeException
        when(fileServiceClient.uploadSubmission(file))
                .thenThrow(new RuntimeException("Upload error"));

        // Act & Assert: Expect RuntimeException with message containing "Upload error"
        assertThatThrownBy(() -> submissionService.submitWork(
                userId, "PARTICIPANT", competitionId, "Title", "Desc", file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload error");
    }

    @Test
    @DisplayName("✅ Should submit team work successfully")
    void testSubmitTeamWork_Success() {
        // Arrange
        String userId = "user-1";
        String competitionId = "comp-1";
        String teamId = "team-1";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filename.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        // Mock competition info: ongoing and not ended
        CompetitionResponseVO competition = new CompetitionResponseVO();
        competition.setStatus(CompetitionStatus.ONGOING);
        competition.setEndDate(LocalDateTime.now().plusDays(5));
        when(competitionServiceClient.getCompetitionById(competitionId))
                .thenReturn(ResponseEntity.ok(competition));

        // Mock file upload returns a valid URL
        when(fileServiceClient.uploadSubmission(file))
                .thenReturn(ResponseEntity.ok("http://mockurl.com/file"));

        // Mock user is in the team
        when(userServiceClient.isUserInTeam(userId, teamId))
                .thenReturn(ResponseEntity.ok(true));

        // Mock user brief info
        when(userServiceClient.getUserBriefById(userId))
                .thenReturn(ResponseEntity.ok(new UserBriefVO())); // Important: avoid NullPointerException

        // Mock successful insert into DB
        when(submissionRecordsMapper.insert(any(SubmissionRecords.class)))
                .thenReturn(1);

        // Act & Assert
        assertThatCode(() -> submissionService.submitTeamWork(
                userId, "PARTICIPANT", competitionId, teamId, "Title", "Desc", file))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should list public approved team submissions successfully")
    void testListPublicApprovedTeamSubmissions_Success() {
        // Arrange: Mock a LambdaQueryChainWrapper for submission query
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);

        // Mock the chain calls properly: eq -> eq -> eq -> isNotNull -> list
        when(submissionService.lambdaQuery()).thenReturn(queryMock);
        when(queryMock.eq(any(), any())).thenReturn(queryMock);           // allow chaining eq()
        when(queryMock.isNotNull(any())).thenReturn(queryMock);           // allow chaining isNotNull()
        when(queryMock.list()).thenReturn(Collections.emptyList());       // simulate empty list returned

        // Act & Assert: Expect no exception thrown
        assertThatCode(() -> submissionService.listPublicApprovedTeamSubmissions(
                "comp-1", 1, 10, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should list public approved submissions successfully")
    void testListPublicApprovedSubmissions_Success() {
        // Arrange: Mock the LambdaQueryChainWrapper for submission records
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock chainable methods: eq(), orderByDesc(), and list()
        when(queryMock.eq(any(), any())).thenReturn(queryMock); // Support multiple eq() calls
        when(queryMock.orderByDesc(any(SFunction.class))).thenReturn(queryMock); // Support orderByDesc()
        when(queryMock.list()).thenReturn(Collections.emptyList()); // Simulate no results

        // Act & Assert: Expect no exception thrown
        assertThatCode(() -> submissionService.listPublicApprovedSubmissions(
                "comp-1", 1, 10, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get submission statistics successfully")
    void testGetSubmissionStatistics_Success() {
        // Arrange
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock chainable methods:
        when(queryMock.eq(any(), any())).thenReturn(queryMock); // Allow eq() chaining
        when(queryMock.count()).thenReturn(0L); // Return a dummy count to avoid NPE

        // Act & Assert
        assertThatCode(() -> submissionService.getSubmissionStatistics("comp-1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get submission scores by team successfully")
    void testGetSubmissionScoresByTeam_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for in() and list()
        when(queryMock.in(any(), anyCollection())).thenReturn(queryMock);
        when(queryMock.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> submissionService.getSubmissionScoresByTeam(Collections.emptyList(), Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get platform submission trend successfully")
    void testGetPlatformSubmissionTrend_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for select() and list()
        when(queryMock.select((SFunction<SubmissionRecords, ?>[]) any())).thenReturn(queryMock);
        when(queryMock.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> submissionService.getPlatformSubmissionTrend())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get submission status by team successfully")
    void testGetSubmissionStatusByTeam_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for in() and list()
        when(queryMock.in(any(), anyCollection())).thenReturn(queryMock);
        when(queryMock.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> submissionService.getSubmissionStatusByTeam(Collections.emptyList(), Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get submission scores successfully")
    void testGetSubmissionScores_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for in() and list()
        when(queryMock.in(any(), anyCollection())).thenReturn(queryMock);
        when(queryMock.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> submissionService.getSubmissionScores("comp-1", Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should get submission status successfully")
    void testGetSubmissionStatus_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for in() and list()
        when(queryMock.in(any(), anyCollection())).thenReturn(queryMock);
        when(queryMock.list()).thenReturn(Collections.emptyList());

        assertThatCode(() -> submissionService.getSubmissionStatus("comp-1", Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ Should delete submissions by user and competition successfully")
    void testDeleteSubmissionsByUserAndCompetition_Success() {
        LambdaQueryChainWrapper<SubmissionRecords> queryMock = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(queryMock);

        // Mock for eq() and delete()
        when(queryMock.eq(any(), any())).thenReturn(queryMock);
        when(submissionRecordsMapper.delete(any())).thenReturn(1);

        assertThatCode(() -> submissionService.deleteSubmissionsByUserAndCompetition("user-1", "comp-1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("✅ getMySubmission returns VO when participant has submitted")
    void testGetMySubmission_Success() {
        // 1) Prepare a fake SubmissionRecords
        SubmissionRecords rec = new SubmissionRecords()
                .setId("s1")
                .setCompetitionId("c1")
                .setUserId("u1")
                .setTitle("T")
                .setDescription("D")
                .setFileUrl("url");

        // 2) Mock the lambdaQuery chain
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> query = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(query);

        // loosen the SFunction matcher to any(), but still check the value
        when(query.eq(any(SFunction.class), eq("c1"))).thenReturn(query);
        when(query.eq(any(SFunction.class), eq("u1"))).thenReturn(query);
        when(query.one()).thenReturn(rec);

        // 3) Exercise
        SubmissionInfoVO vo = submissionService.getMySubmission("c1", "u1", "PARTICIPANT");

        // 4) Verify
        assertThat(vo.getId()).isEqualTo("s1");
    }

    @Test
    @DisplayName("❌ getMySubmission forbidden if role wrong")
    void testGetMySubmission_Forbidden() {
        assertThatThrownBy(() ->
                submissionService.getMySubmission("c1","u1","ADMIN")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PARTICIPANT");
    }

    @Test
    @DisplayName("❌ getMySubmission not found")
    void testGetMySubmission_NotFound() {
        // 1) Create a mock query and have the spy return it
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(submissionService).lambdaQuery();

        // 2) Stub the two eq(...) calls to return the same mock
        when(query.eq(any(SFunction.class), any())).thenReturn(query);
        // (you only need this once—Mockito will use it for both eq() invocations)

        // 3) Stub the final .one() to return null
        when(query.one()).thenReturn(null);

        // 4) Now exercise and verify we get the expected BusinessException
        assertThatThrownBy(() ->
                submissionService.getMySubmission("c1", "u1", "PARTICIPANT")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No submission found");
    }

    @Test
    @DisplayName("❌ listSubmissionsByRole forbidden if not organizer/admin")
    void testListSubmissionsByRole_Forbidden() {
        when(competitionOrganizersService.lambdaQuery().exists()).thenReturn(false);
        assertThatThrownBy(() ->
                submissionService.listSubmissionsByRole("c1","u1","PARTICIPANT",1,10,null,null,null)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("❌ listTeamSubmissionsByRole forbidden if not admin/org")
    void testListTeamSubmissionsByRole_Forbidden() {
        LambdaQueryChainWrapper<CompetitionOrganizers> oq = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(oq);
        when(oq.eq(any(), any())).thenReturn(oq);
        when(oq.exists()).thenReturn(false);

        assertThatThrownBy(() ->
                submissionService.listTeamSubmissionsByRole("c1","u1","PARTICIPANT",1,10,null,null,null)
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only organizers or admins");
    }

    @Test
    @DisplayName("✅ reviewSubmission happy path")
    void testReviewSubmission_Success() {
        // 1) stub getById including a userId so we know who to notify
        SubmissionRecords sub = new SubmissionRecords()
                .setId("s1")
                .setCompetitionId("c1")
                .setUserId("u1")       // must set this or notifyUserId will be null
                .setTitle("T");
        doReturn(sub).when(submissionService).getById("s1");

        // 2) spy out updateById so it returns true
        doReturn(true).when(submissionService).updateById(any(SubmissionRecords.class));

        // 3) stub fetching competition info
        CompetitionResponseVO comp = new CompetitionResponseVO();
        comp.setName("Comp");
        when(competitionServiceClient.getCompetitionById("c1"))
                .thenReturn(ResponseEntity.ok(comp));

        // 4) stub reviewer info
        UserBriefVO reviewer = new UserBriefVO();
        reviewer.setName("R");
        reviewer.setEmail("r@e");
        when(userServiceClient.getUserBriefById("rev"))
                .thenReturn(ResponseEntity.ok(reviewer));

        // 5) stub submitter info (must match submission.getUserId())
        UserBriefVO submitter = new UserBriefVO();
        submitter.setName("S");
        submitter.setEmail("s@e");
        when(userServiceClient.getUserBriefById("u1"))
                .thenReturn(ResponseEntity.ok(submitter));

        // 6) build DTO and invoke
        SubmissionReviewDTO dto = new SubmissionReviewDTO();
        dto.setSubmissionId("s1");
        dto.setReviewStatus("APPROVED");
        dto.setReviewComments("ok");

        // Should not throw any exception
        assertThatCode(() ->
                submissionService.reviewSubmission(dto, "rev", "ADMIN")
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("❌ reviewSubmission invalid status")
    void testReviewSubmission_InvalidStatus() {
        doReturn(new SubmissionRecords().setId("s1").setCompetitionId("c1"))
                .when(submissionService).getById("s1");
        SubmissionReviewDTO dto = new SubmissionReviewDTO();
        dto.setSubmissionId("s1");
        dto.setReviewStatus("WRONG");
        assertThatThrownBy(() ->
                submissionService.reviewSubmission(dto,"u","ADMIN")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid review status");
    }

    @Test
    @DisplayName("❌ reviewSubmission not found")
    void testReviewSubmission_NotFound() {
        // stub getById to return null
        doReturn(null).when(submissionService).getById("bad");

        // build DTO in two steps because setSubmissionId(...) is void
        SubmissionReviewDTO dto = new SubmissionReviewDTO();
        dto.setSubmissionId("bad");

        // assert that a BusinessException is thrown with the expected message
        assertThatThrownBy(() ->
                submissionService.reviewSubmission(dto, "u", "ADMIN")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Submission not found");
    }

    @Test
    @DisplayName("✅ isUserOrganizerOfSubmission returns true")
    void testIsUserOrganizerOfSubmission_Success() {
        SubmissionRecords sr = new SubmissionRecords().setCompetitionId("c1");
        doReturn(sr).when(submissionService).getById("s1");
        LambdaQueryChainWrapper<CompetitionOrganizers> oq = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery().exists()).thenReturn(true);
        assertThat(submissionService.isUserOrganizerOfSubmission("s1","u")).isTrue();
    }

    @Test
    @DisplayName("❌ isUserOrganizerOfSubmission not found")
    void testIsUserOrganizerOfSubmission_NotFound() {
        doReturn(null).when(submissionService).getById("s1");
        assertThatThrownBy(() ->
                submissionService.isUserOrganizerOfSubmission("s1","u")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Submission not found");
    }

    @Test
    @DisplayName("❌ deleteSubmission forbidden if not allowed")
    void testDeleteSubmission_Forbidden() {
        SubmissionRecords sr = new SubmissionRecords()
                .setId("s1").setUserId("u").setCompetitionId("c1").setFileUrl(null);
        doReturn(sr).when(submissionService).getById("s1");
        when(competitionOrganizersService.lambdaQuery().exists()).thenReturn(false);

        assertThatThrownBy(() ->
                submissionService.deleteSubmission("s1","other","PARTICIPANT")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("not allowed to delete");
    }

    @Test
    @DisplayName("❌ deleteSubmission not found")
    void testDeleteSubmission_NotFound() {
        doReturn(null).when(submissionService).getById("bad");
        assertThatThrownBy(() ->
                submissionService.deleteSubmission("bad","u","ADMIN")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Submission not found");
    }

    @Test
    @DisplayName("❌ existsByTeamId blank id")
    void testExistsByTeamId_Blank() {
        assertThatThrownBy(() ->
                submissionService.existsByTeamId(" ")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("❌ getSubmissionTrend blank id")
    void testGetSubmissionTrend_Blank() {
        assertThatThrownBy(() ->
                submissionService.getSubmissionTrend(" ")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("❌ getSubmissionTrend not found competition")
    void testGetSubmissionTrend_CompNotFound() {
        when(competitionServiceClient.getCompetitionById("c1")).thenReturn(ResponseEntity.ok(null));
        assertThatThrownBy(() ->
                submissionService.getSubmissionTrend("c1")
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Competition not found");
    }

    @Test
    @DisplayName("✅ getSubmissionTrend success")
    void testGetSubmissionTrend_Success() {
        // 1) Stub that the competition exists
        when(competitionServiceClient.getCompetitionById("c1"))
                .thenReturn(ResponseEntity.ok(new CompetitionResponseVO()));

        // 2) Prepare a dummy SubmissionRecord
        SubmissionRecords r = new SubmissionRecords().setCreatedAt(LocalDateTime.now());

        // 3) Mock the lambdaQuery chain
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(q);

        // 4) Stub eq(...) to return the same mock
        when(q.eq(any(), eq("c1"))).thenReturn(q);

        // 5) Stub select(...) (varargs) to return the same mock
        when(q.select(any(com.baomidou.mybatisplus.core.toolkit.support.SFunction[].class)))
                .thenReturn(q);

        // 6) Stub list() to return our one-record list
        when(q.list()).thenReturn(List.of(r));

        // 7) Exercise and verify
        Map<String, Integer> trend = submissionService.getSubmissionTrend("c1");
        String dateKey = r.getCreatedAt().toLocalDate().toString();
        assertThat(trend).containsKey(dateKey);
    }

    @Test
    @DisplayName("✅ getPlatformSubmissionStatistics empty")
    void testGetPlatformSubmissionStatistics_Empty() {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(q);
        when(q.select((SFunction<SubmissionRecords, ?>[]) any(SFunction[].class))).thenReturn(q);
        when(q.list()).thenReturn(Collections.emptyList());

        var stats = submissionService.getPlatformSubmissionStatistics();
        assertThat(stats.getTotalSubmissions()).isZero();
        assertThat(stats.getApprovedSubmissions()).isZero();
        assertThat(stats.getIndividualSubmissions()).isZero();
        assertThat(stats.getTeamSubmissions()).isZero();
    }

    @Test
    @DisplayName("✅ getPlatformSubmissionStatistics non-empty")
    void testGetPlatformSubmissionStatistics_NonEmpty() {
        // three submissions: one approved individual (a), one pending individual (b), one rejected team (c)
        SubmissionRecords a = new SubmissionRecords().setReviewStatus("APPROVED");
        SubmissionRecords b = new SubmissionRecords().setReviewStatus("PENDING").setTeamId(null);
        SubmissionRecords c = new SubmissionRecords().setReviewStatus("REJECTED").setTeamId("t1");

        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(q);
        when(q.select((SFunction<SubmissionRecords, ?>[]) any(SFunction[].class))).thenReturn(q);
        when(q.list()).thenReturn(List.of(a, b, c));

        var stats = submissionService.getPlatformSubmissionStatistics();
        assertThat(stats.getTotalSubmissions()).isEqualTo(3);
        assertThat(stats.getApprovedSubmissions()).isEqualTo(1);
        assertThat(stats.getIndividualSubmissions()).isEqualTo(2);  // now correctly expecting 2
        assertThat(stats.getTeamSubmissions()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ getPlatformSubmissionTrend empty")
    void testGetPlatformSubmissionTrend_Empty() {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(q);
        when(q.select((SFunction<SubmissionRecords, ?>[]) any(SFunction[].class))).thenReturn(q);
        when(q.list()).thenReturn(Collections.emptyList());

        var trend = submissionService.getPlatformSubmissionTrend();
        assertThat(trend).isEmpty();
    }

    @Test
    @DisplayName("✅ getPlatformSubmissionTrend success")
    void testGetPlatformSubmissionTrend_NonEmpty() {
        SubmissionRecords r1 = new SubmissionRecords().setCreatedAt(LocalDateTime.now().minusDays(1));
        SubmissionRecords r2 = new SubmissionRecords().setCreatedAt(LocalDateTime.now());

        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);
        when(submissionService.lambdaQuery()).thenReturn(q);
        when(q.select((SFunction<SubmissionRecords, ?>[]) any(SFunction[].class))).thenReturn(q);
        when(q.list()).thenReturn(List.of(r1, r2));

        var trend = submissionService.getPlatformSubmissionTrend();
        // should contain an entry for r1's date
        String key1 = r1.getCreatedAt().toLocalDate().toString();
        assertThat(trend).containsKey(key1);
        assertThat(trend.get(key1)).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ getSubmissionStatistics throws on blank id")
    void testGetSubmissionStatistics_Blank() {
        assertThatThrownBy(() -> submissionService.getSubmissionStatistics(" "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("✅ getSubmissionStatistics success")
    void testGetSubmissionStatistics_Happy() {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<SubmissionRecords> q = mock(LambdaQueryChainWrapper.class);

        // Always return our same mock for any lambdaQuery() call
        when(submissionService.lambdaQuery()).thenReturn(q);

        // And have every .eq(...) return the same chainable mock
        when(q.eq(any(), any())).thenReturn(q);

        // Then stub .count() to return 5 (total), 2 (approved), 1 (pending), 2 (rejected) in that order
        when(q.count()).thenReturn(5L, 2L, 1L, 2L);

        SubmissionStatisticsVO vo = submissionService.getSubmissionStatistics("c1");

        assertThat(vo.getTotalSubmissions()).isEqualTo(5);
        assertThat(vo.getApprovedSubmissions()).isEqualTo(2);
        assertThat(vo.getPendingSubmissions()).isEqualTo(1);
        assertThat(vo.getRejectedSubmissions()).isEqualTo(2);
    }

}
