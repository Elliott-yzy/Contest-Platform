package com.w16a.danish.interaction.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.w16a.danish.interaction.domain.dto.SubmissionCommentDTO;
import com.w16a.danish.interaction.domain.po.SubmissionComments;
import com.w16a.danish.interaction.domain.vo.PageResponse;
import com.w16a.danish.interaction.domain.vo.SubmissionCommentVO;
import com.w16a.danish.interaction.exception.BusinessException;
import com.w16a.danish.interaction.feign.RegistrationServiceClient;
import com.w16a.danish.interaction.feign.UserServiceClient;
import com.w16a.danish.interaction.mapper.SubmissionCommentsMapper;
import com.w16a.danish.interaction.domain.vo.UserBriefVO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SubmissionCommentsServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionCommentsServiceImpl submissionCommentsService;

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private SubmissionCommentsMapper submissionCommentsMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        var baseMapperField = SubmissionCommentsServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(submissionCommentsService, submissionCommentsMapper);

        LambdaQueryChainWrapper<SubmissionComments> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(submissionCommentsService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);

        when(submissionCommentsMapper.insert(any(SubmissionComments.class))).thenReturn(1);
        when(submissionCommentsMapper.updateById(any(SubmissionComments.class))).thenReturn(1);
    }

    @Test
    @DisplayName("✅ Add comment successfully")
    void testAddCommentSuccess() {
        SubmissionCommentDTO dto = new SubmissionCommentDTO();
        dto.setSubmissionId("submissionId");
        dto.setContent("Nice work!");

        submissionCommentsService.addComment("userId", dto);

        verify(submissionCommentsMapper, times(1)).insert(any(SubmissionComments.class));
    }


    @Test
    @DisplayName("✅ Delete comment as ADMIN")
    void testDeleteCommentByAdmin() {
        SubmissionComments comment = new SubmissionComments().setUserId("otherUser");
        when(submissionCommentsService.getById(anyString())).thenReturn(comment);
        when(submissionCommentsService.removeById(anyString())).thenReturn(true);

        submissionCommentsService.deleteComment("commentId", "adminUser", "ADMIN");

        verify(submissionCommentsService).removeById("commentId");
    }

    @Test
    @DisplayName("✅ Delete comment as OWNER")
    void testDeleteCommentByOwner() {
        SubmissionComments comment = new SubmissionComments().setUserId("ownerUser");
        when(submissionCommentsService.getById(anyString())).thenReturn(comment);
        when(submissionCommentsService.removeById(anyString())).thenReturn(true);

        submissionCommentsService.deleteComment("commentId", "ownerUser", "PARTICIPANT");

        verify(submissionCommentsService).removeById("commentId");
    }

    @Test
    @DisplayName("✅ Delete comment as ORGANIZER")
    void testDeleteCommentByOrganizer() {
        SubmissionComments comment = new SubmissionComments().setUserId("otherUser").setSubmissionId("submissionId");
        when(submissionCommentsService.getById(anyString())).thenReturn(comment);
        when(registrationServiceClient.isUserOrganizerOfSubmission(anyString(), anyString())).thenReturn(true);
        when(submissionCommentsService.removeById(anyString())).thenReturn(true);

        submissionCommentsService.deleteComment("commentId", "organizerUser", "PARTICIPANT");

        verify(submissionCommentsService).removeById("commentId");
    }

    @Test
    @DisplayName("❌ Delete comment unauthorized")
    void testDeleteCommentUnauthorized() {
        SubmissionComments comment = new SubmissionComments().setUserId("otherUser").setSubmissionId("submissionId");
        when(submissionCommentsService.getById(anyString())).thenReturn(comment);
        when(registrationServiceClient.isUserOrganizerOfSubmission(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> submissionCommentsService.deleteComment("commentId", "randomUser", "PARTICIPANT"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not have permission"); // 👈 改成和实际异常一致
    }

    @Test
    @DisplayName("✅ Update comment successfully")
    void testUpdateCommentSuccess() {
        SubmissionComments comment = new SubmissionComments().setUserId("userId");

        when(submissionCommentsService.getById(anyString())).thenReturn(comment);

        SubmissionCommentDTO dto = new SubmissionCommentDTO();
        dto.setContent("Updated Content");

        submissionCommentsService.updateComment("commentId", "userId", dto);

        verify(submissionCommentsMapper, times(1)).updateById(any(SubmissionComments.class));
    }

    @Test
    @DisplayName("❌ Update comment not authorized")
    void testUpdateCommentUnauthorized() {
        SubmissionComments comment = new SubmissionComments().setUserId("otherUser");
        when(submissionCommentsService.getById(anyString())).thenReturn(comment);

        SubmissionCommentDTO dto = new SubmissionCommentDTO();
        dto.setContent("Updated Content");

        assertThatThrownBy(() -> submissionCommentsService.updateComment("commentId", "userId", dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("only edit your own comments");
    }

    @Test
    @DisplayName("✅ Get paginated comments successfully")
    void testGetPaginatedCommentsSuccess() {
        LambdaQueryChainWrapper<SubmissionComments> query = mock(LambdaQueryChainWrapper.class);

        doReturn(query).when(submissionCommentsService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);
        when(query.isNull(any())).thenReturn(query);
        when(query.orderBy(anyBoolean(), anyBoolean(), any(SFunction.class))).thenReturn(query);
        when(query.list()).thenReturn(Collections.emptyList());

        when(userServiceClient.getUsersByIds(anyList(), any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        PageResponse<SubmissionCommentVO> result = submissionCommentsService.getPaginatedComments(
                "submissionId", 1, 10, "createdAt", "desc"
        );

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("✅ Count comments for a submission")
    void testCountCommentsSuccess() {
        when(submissionCommentsService.lambdaQuery().count()).thenReturn(5L);

        long count = submissionCommentsService.countComments("submissionId");

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("✅ Count all comments")
    void testCountAllCommentsSuccess() {
        when(submissionCommentsService.lambdaQuery().count()).thenReturn(10L);

        long count = submissionCommentsService.countAllComments();

        assertThat(count).isEqualTo(10L);
    }

    @Test
    @DisplayName("✅ Get paginated comments with existing users and parent comments")
    void testGetPaginatedComments_WithUsersAndParentComments() {
        // Arrange
        LambdaQueryChainWrapper<SubmissionComments> parentQuery = mock(LambdaQueryChainWrapper.class);
        LambdaQueryChainWrapper<SubmissionComments> replyQuery = mock(LambdaQueryChainWrapper.class);

        doReturn(parentQuery).doReturn(replyQuery).when(submissionCommentsService).lambdaQuery();

        // parent query mock
        when(parentQuery.eq(any(SFunction.class), any())).thenReturn(parentQuery);
        when(parentQuery.isNull(any(SFunction.class))).thenReturn(parentQuery);
        when(parentQuery.orderBy(anyBoolean(), anyBoolean(), any(SFunction.class))).thenReturn(parentQuery);
        when(parentQuery.list()).thenReturn(List.of(
                new SubmissionComments()
                        .setId("parentId")
                        .setContent("Parent Comment")
                        .setUserId("user1")
        ));

        // reply query mock
        when(replyQuery.eq(any(SFunction.class), any())).thenReturn(replyQuery);
        when(replyQuery.in(any(SFunction.class), any(Collection.class))).thenReturn(replyQuery);
        when(replyQuery.orderByAsc(any(SFunction.class))).thenReturn(replyQuery);
        when(replyQuery.list()).thenReturn(List.of(
                new SubmissionComments()
                        .setId("replyId")
                        .setContent("Reply Comment")
                        .setParentId("parentId")
                        .setUserId("user2")
        ));

        when(userServiceClient.getUsersByIds(anyList(), any()))
                .thenReturn(ResponseEntity.ok(List.of(
                        UserBriefVO.builder()
                                .id("user1")
                                .name("User 1")
                                .build(),
                        UserBriefVO.builder()
                                .id("user2")
                                .name("User 2")
                                .build()
                )));

        // Act
        PageResponse<SubmissionCommentVO> result = submissionCommentsService.getPaginatedComments(
                "submissionId", 1, 10, "createdAt", "desc"
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);

        SubmissionCommentVO parent = result.getData().get(0);
        assertThat(parent.getId()).isEqualTo("parentId");

        assertThat(parent.getReplies()).isNotNull();
        assertThat(parent.getReplies()).hasSize(1);

        SubmissionCommentVO reply = parent.getReplies().get(0);
        assertThat(reply.getId()).isEqualTo("replyId");
    }

}
