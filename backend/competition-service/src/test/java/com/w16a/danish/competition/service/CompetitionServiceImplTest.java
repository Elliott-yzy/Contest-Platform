package com.w16a.danish.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w16a.danish.competition.config.CompetitionNotifier;
import com.w16a.danish.competition.domain.dto.AssignJudgesDTO;
import com.w16a.danish.competition.domain.dto.CompetitionCreateDTO;
import com.w16a.danish.competition.domain.po.CompetitionJudges;
import com.w16a.danish.competition.domain.po.CompetitionOrganizers;
import com.w16a.danish.competition.domain.po.Competitions;
import com.w16a.danish.competition.domain.vo.CompetitionResponseVO;
import com.w16a.danish.competition.domain.vo.PageResponse;
import com.w16a.danish.competition.domain.vo.UserBriefVO;
import com.w16a.danish.competition.exception.BusinessException;
import com.w16a.danish.competition.feign.FileServiceClient;
import com.w16a.danish.competition.feign.UserServiceClient;
import com.w16a.danish.competition.mapper.CompetitionsMapper;
import com.w16a.danish.competition.service.impl.CompetitionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompetitionServiceImplTest {

    @Spy
    @InjectMocks
    private CompetitionsServiceImpl competitionsService;


    @Mock private FileServiceClient fileServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private ICompetitionOrganizersService competitionOrganizersService;
    @Mock private ICompetitionJudgesService competitionJudgesService;
    @Mock private CompetitionNotifier competitionNotifier;
    @Mock private CompetitionsMapper competitionsMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        var baseMapperField = CompetitionsServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(competitionsService, competitionsMapper);

        LambdaQueryChainWrapper<Competitions> mockQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(mockQuery).when(competitionsService).lambdaQuery();
        when(mockQuery.eq(any(), any())).thenReturn(mockQuery);
        when(mockQuery.in(any(), anyCollection())).thenReturn(mockQuery);
        when(mockQuery.exists()).thenReturn(false);
        when(mockQuery.list()).thenReturn(Collections.emptyList());
    }


    @Test
    @DisplayName("✅ Create competition success")
    void testCreateCompetitionSuccess() {
        CompetitionCreateDTO dto = new CompetitionCreateDTO();
        dto.setName("Test Competition");

        // mock competitionsService.lambdaQuery()
        LambdaQueryChainWrapper<Competitions> competitionQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(competitionQuery).when(competitionsService).lambdaQuery();
        when(competitionQuery.eq(any(), any())).thenReturn(competitionQuery);
        when(competitionQuery.exists()).thenReturn(false);

        CompetitionResponseVO response = competitionsService.createCompetition(dto, "ADMIN", "userId");

        assertThat(response).isNotNull();
        verify(competitionsMapper).insert(any(Competitions.class));
        verify(competitionOrganizersService).save(any());
    }


    @Test
    @DisplayName("❌ Create competition - Duplicate name")
    void testCreateCompetitionDuplicateName() {
        CompetitionCreateDTO dto = new CompetitionCreateDTO();
        dto.setName("Duplicate Competition");

        LambdaQueryChainWrapper<Competitions> competitionQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(competitionQuery).when(competitionsService).lambdaQuery();
        when(competitionQuery.eq(any(), any())).thenReturn(competitionQuery);
        when(competitionQuery.exists()).thenReturn(true);

        assertThatThrownBy(() -> competitionsService.createCompetition(dto, "ADMIN", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }


    @Test
    @DisplayName("✅ Delete competition success")
    void testDeleteCompetitionSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);
        when(competitionsMapper.deleteById(anyString())).thenReturn(1);

        competitionsService.deleteCompetition("comp-id", "ADMIN", "userId");

        verify(competitionsMapper).deleteById("comp-id");
    }

    @Test
    @DisplayName("❌ Delete competition - No permission")
    void testDeleteCompetitionNoPermission() {
        assertThatThrownBy(() -> competitionsService.deleteCompetition("comp-id", "PARTICIPANT", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("authorized");
    }

    @Test
    @DisplayName("✅ Get competition by ID success")
    void testGetCompetitionByIdSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);

        CompetitionResponseVO result = competitionsService.getCompetitionById("comp-id");

        assertThat(result.getId()).isEqualTo("comp-id");
    }

    @Test
    @DisplayName("❌ Get competition by ID - Not found")
    void testGetCompetitionByIdNotFound() {
        when(competitionsMapper.selectById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> competitionsService.getCompetitionById("comp-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("✅ Upload competition media success")
    void testUploadCompetitionMedia() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);
        when(fileServiceClient.uploadCompetitionPromo(any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok("http://mocked.com/uploaded.mp4"));

        MockMultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "test".getBytes());

        CompetitionResponseVO response = competitionsService.uploadCompetitionMedia("comp-id", "userId", "ADMIN", "VIDEO", file);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("✅ Delete intro video success")
    void testDeleteIntroVideoSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");
        competition.setIntroVideoUrl("http://mocked.com/oldvideo.mp4");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);
        when(competitionsMapper.update(any(), any())).thenReturn(1);

        CompetitionResponseVO response = competitionsService.deleteIntroVideo("comp-id", "userId", "ADMIN");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("✅ Assign judges success")
    void testAssignJudgesSuccess() {
        AssignJudgesDTO dto = new AssignJudgesDTO();
        dto.setJudgeEmails(List.of("test@example.com"));

        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);

        when(userServiceClient.getUsersByEmails(anyList()))
                .thenReturn(ResponseEntity.ok(List.of(
                        UserBriefVO.builder()
                                .id("userId")
                                .name("Judge Name")
                                .email("test@example.com")
                                .build()
                )));

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        LambdaQueryChainWrapper<CompetitionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionJudgesService.lambdaQuery()).thenReturn(judgeQuery);
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        doReturn(judgeQuery).when(judgeQuery).select((SFunction<CompetitionJudges, ?>) any());
        when(judgeQuery.list()).thenReturn(Collections.emptyList());

        when(competitionJudgesService.saveBatch(anyList())).thenReturn(true);

        competitionsService.assignJudges("comp-id", "userId", "ADMIN", dto);

        verify(competitionNotifier, atLeastOnce()).sendJudgeAssigned(any());
    }

    @Test
    @DisplayName("✅ Remove judge success")
    void testRemoveJudgeSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsMapper.selectById(anyString())).thenReturn(competition);

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        LambdaQueryChainWrapper<CompetitionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionJudgesService.lambdaQuery()).thenReturn(judgeQuery);
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);
        when(judgeQuery.exists()).thenReturn(true);

        LambdaUpdateChainWrapper<CompetitionJudges> updateQuery = mock(LambdaUpdateChainWrapper.class);
        when(competitionJudgesService.lambdaUpdate()).thenReturn(updateQuery);
        when(updateQuery.eq(any(), any())).thenReturn(updateQuery);
        when(updateQuery.remove()).thenReturn(true);

        when(userServiceClient.getUserBriefById(anyString()))
                .thenReturn(ResponseEntity.ok(
                        UserBriefVO.builder()
                                .id("userId")
                                .name("Judge Name")
                                .email("judge@example.com")
                                .build()
                ));

        competitionsService.removeJudge("comp-id", "userId", "ADMIN", "judgeId");

        verify(competitionNotifier).sendJudgeRemoved(any());
    }

    @Test
    @DisplayName("✅ listAllCompetitions returns competitions")
    void testListAllCompetitionsSuccess() {
        // mock competition
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        LambdaQueryChainWrapper<Competitions> queryWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(queryWrapper).when(competitionsService).lambdaQuery();
        when(queryWrapper.eq(any(), any())).thenReturn(queryWrapper);
        doReturn(queryWrapper).when(queryWrapper).orderByDesc(any(SFunction.class));
        when(queryWrapper.list()).thenReturn(List.of(competition));

        List<CompetitionResponseVO> result = competitionsService.listAllCompetitions();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo("comp-id");
    }

    @Test
    @DisplayName("✅ listAllCompetitions returns empty")
    void testListAllCompetitionsEmpty() {
        LambdaQueryChainWrapper<Competitions> queryWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(queryWrapper).when(competitionsService).lambdaQuery();
        when(queryWrapper.eq(any(), any())).thenReturn(queryWrapper);
        doReturn(queryWrapper).when(queryWrapper).orderByDesc(any(SFunction.class));
        when(queryWrapper.list()).thenReturn(Collections.emptyList());

        List<CompetitionResponseVO> result = competitionsService.listAllCompetitions();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("✅ updateCompetitionStatus success")
    void testUpdateCompetitionStatusSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsService.getById(anyString())).thenReturn(competition);
        when(competitionsService.updateById(any())).thenReturn(true);

        CompetitionResponseVO result = competitionsService.updateCompetitionStatus("comp-id", "ONGOING");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("❌ updateCompetitionStatus - invalid status")
    void testUpdateCompetitionStatusInvalidStatus() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");

        when(competitionsService.getById(anyString())).thenReturn(competition);

        assertThatThrownBy(() -> competitionsService.updateCompetitionStatus("comp-id", "INVALID_STATUS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid competition status");
    }

    @Test
    @DisplayName("❌ updateCompetitionStatus - competition not found")
    void testUpdateCompetitionStatusNotFound() {
        when(competitionsService.getById(anyString())).thenReturn(null);

        assertThatThrownBy(() -> competitionsService.updateCompetitionStatus("comp-id", "ONGOING"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Competition not found");
    }

    @Test
    @DisplayName("✅ deleteCompetitionImage - success")
    void testDeleteCompetitionImageSuccess() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");
        competition.setImageUrls(new ArrayList<>(List.of("http://mock.com/image.jpg")));

        when(competitionsService.getById(anyString())).thenReturn(competition);

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        CompetitionResponseVO response = competitionsService.deleteCompetitionImage(
                "comp-id", "userId", "ORGANIZER", "http://mock.com/image.jpg"
        );

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("comp-id");
    }


    @Test
    @DisplayName("❌ deleteCompetitionImage - not authorized")
    void testDeleteCompetitionImageUnauthorized() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");
        competition.setImageUrls(List.of("http://mock.com/img1.jpg"));

        when(competitionsService.getById(anyString())).thenReturn(competition);

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(false);

        assertThatThrownBy(() -> competitionsService.deleteCompetitionImage("comp-id", "userId", "ORGANIZER", "http://mock.com/img1.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("❌ deleteCompetitionImage - image not found")
    void testDeleteCompetitionImageNotFound() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");
        competition.setImageUrls(new ArrayList<>(List.of("http://mock.com/img1.jpg")));

        when(competitionsService.getById(anyString())).thenReturn(competition);

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        assertThatThrownBy(() -> competitionsService.deleteCompetitionImage(
                "comp-id", "userId", "ORGANIZER", "http://mock.com/non-exist.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Image URL not found");
    }



    @Test
    @DisplayName("✅ isUserOrganizer true")
    void testIsUserOrganizerTrue() {
        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        boolean result = competitionsService.isUserOrganizer("comp-id", "userId");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ isUserOrganizer false")
    void testIsUserOrganizerFalse() {
        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(false);

        boolean result = competitionsService.isUserOrganizer("comp-id", "userId");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("✅ listCompetitions with keyword and filters")
    void testListCompetitionsWithFilters() {
        Page<Competitions> page = new Page<>(1, 10);
        when(competitionsService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageResponse<CompetitionResponseVO> result = competitionsService.listCompetitions(
                "hackathon", "UPCOMING", "AI", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("✅ listCompetitions without filters")
    void testListCompetitionsWithoutFilters() {
        Page<Competitions> page = new Page<>(1, 10);
        when(competitionsService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageResponse<CompetitionResponseVO> result = competitionsService.listCompetitions(
                null, null, null, 1, 10);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("✅ listCompetitionsByOrganizer returns empty when no competitions")
    void testListCompetitionsByOrganizerEmpty() {
        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.list()).thenReturn(Collections.emptyList());

        PageResponse<CompetitionResponseVO> result = competitionsService.listCompetitionsByOrganizer(
                "userId", "ORGANIZER", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("✅ listCompetitionsByOrganizer returns competitions")
    void testListCompetitionsByOrganizerSuccess() {
        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);

        CompetitionOrganizers organizer = new CompetitionOrganizers();
        organizer.setCompetitionId("comp-id");
        when(organizerQuery.list()).thenReturn(List.of(organizer));

        Page<Competitions> page = new Page<>(1, 10);
        when(competitionsService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageResponse<CompetitionResponseVO> result = competitionsService.listCompetitionsByOrganizer(
                "userId", "ORGANIZER", 1, 10);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("✅ listAssignedJudges returns empty")
    void testListAssignedJudgesEmpty() {
        Competitions competition = new Competitions();
        competition.setId("comp-id");
        when(competitionsService.getById(anyString())).thenReturn(competition);

        LambdaQueryChainWrapper<CompetitionOrganizers> organizerQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionOrganizersService.lambdaQuery()).thenReturn(organizerQuery);
        when(organizerQuery.eq(any(), any())).thenReturn(organizerQuery);
        when(organizerQuery.exists()).thenReturn(true);

        LambdaQueryChainWrapper<CompetitionJudges> judgeQuery = mock(LambdaQueryChainWrapper.class);
        when(competitionJudgesService.lambdaQuery()).thenReturn(judgeQuery);
        when(judgeQuery.eq(any(), any())).thenReturn(judgeQuery);

        Page<CompetitionJudges> judgePage = new Page<>(1, 10);
        when(judgeQuery.page(any(Page.class))).thenReturn(judgePage);

        PageResponse<UserBriefVO> result = competitionsService.listAssignedJudges(
                "comp-id", "userId", "ORGANIZER", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }


    @Test
    @DisplayName("✅ getCompetitionsByIds returns empty if ids empty")
    void testGetCompetitionsByIdsEmpty() {
        List<CompetitionResponseVO> result = competitionsService.getCompetitionsByIds(Collections.emptyList());
        assertThat(result).isEmpty();
    }


}
