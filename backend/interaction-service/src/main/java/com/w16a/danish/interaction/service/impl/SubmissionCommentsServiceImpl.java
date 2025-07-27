package com.w16a.danish.interaction.service.impl;

import cn.hutool.core.util.StrUtil;
import com.w16a.danish.interaction.domain.dto.SubmissionCommentDTO;
import com.w16a.danish.interaction.domain.po.SubmissionComments;
import com.w16a.danish.interaction.domain.vo.PageResponse;
import com.w16a.danish.interaction.domain.vo.SubmissionCommentVO;
import com.w16a.danish.interaction.domain.vo.UserBriefVO;
import com.w16a.danish.interaction.exception.BusinessException;
import com.w16a.danish.interaction.feign.RegistrationServiceClient;
import com.w16a.danish.interaction.feign.UserServiceClient;
import com.w16a.danish.interaction.mapper.SubmissionCommentsMapper;
import com.w16a.danish.interaction.service.ISubmissionCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * SubmissionCommentsServiceImpl
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@Service
@RequiredArgsConstructor
public class SubmissionCommentsServiceImpl extends ServiceImpl<SubmissionCommentsMapper, SubmissionComments> implements ISubmissionCommentsService {

    private final RegistrationServiceClient registrationServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public void addComment(String userId, SubmissionCommentDTO dto) {
        SubmissionComments comment = new SubmissionComments()
                .setId(StrUtil.uuid())
                .setSubmissionId(dto.getSubmissionId())
                .setUserId(userId)
                .setParentId(StrUtil.isBlank(dto.getParentId()) ? null : dto.getParentId())
                .setContent(dto.getContent());

        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save comment");
        }
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String userId, String userRole) {
        SubmissionComments comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Comment not found");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOwner = userId.equals(comment.getUserId());
        boolean isOrganizer = false;

        if (!isAdmin && !isOwner) {
            isOrganizer = Boolean.TRUE.equals(registrationServiceClient.isUserOrganizerOfSubmission(
                    comment.getSubmissionId(), userId));
        }

        if (!(isAdmin || isOwner || isOrganizer)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You do not have permission to delete this comment");
        }

        boolean removed = this.removeById(commentId);
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete comment");
        }
    }

    @Override
    public PageResponse<SubmissionCommentVO> getPaginatedComments(String submissionId, int page, int size, String sortBy, String order) {
        if (!"createdAt".equalsIgnoreCase(sortBy) && !"updatedAt".equalsIgnoreCase(sortBy)) {
            sortBy = "createdAt";
        }
        boolean isAsc = "asc".equalsIgnoreCase(order);

        List<SubmissionComments> topLevelComments = this.lambdaQuery()
                .eq(SubmissionComments::getSubmissionId, submissionId)
                .isNull(SubmissionComments::getParentId)
                .orderBy(true, isAsc, "createdAt".equalsIgnoreCase(sortBy) ? SubmissionComments::getCreatedAt : SubmissionComments::getUpdatedAt)
                .list();

        int total = topLevelComments.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SubmissionComments> paged = topLevelComments.subList(fromIndex, toIndex);

        List<SubmissionComments> childComments = Collections.emptyList();
        List<String> parentIds = paged.stream().map(SubmissionComments::getId).toList();
        if (!parentIds.isEmpty()) {
            childComments = this.lambdaQuery()
                    .eq(SubmissionComments::getSubmissionId, submissionId)
                    .in(SubmissionComments::getParentId, parentIds)
                    .orderByAsc(SubmissionComments::getCreatedAt)
                    .list();
        }

        Set<String> allUserIds = new HashSet<>();
        paged.forEach(c -> allUserIds.add(c.getUserId()));
        childComments.forEach(c -> allUserIds.add(c.getUserId()));

        Map<String, UserBriefVO> userMap = Optional.ofNullable(
                userServiceClient.getUsersByIds(new ArrayList<>(allUserIds), null).getBody()
        ).orElse(List.of()).stream().collect(Collectors.toMap(UserBriefVO::getId, u -> u));

        Map<String, List<SubmissionCommentVO>> groupedReplies = childComments.stream()
                .map(c -> {
                    SubmissionCommentVO vo = new SubmissionCommentVO();
                    vo.setId(c.getId());
                    vo.setSubmissionId(c.getSubmissionId());
                    vo.setParentId(c.getParentId());
                    vo.setContent(c.getContent());
                    vo.setUserId(c.getUserId());
                    vo.setCreatedAt(c.getCreatedAt());
                    vo.setUpdatedAt(c.getUpdatedAt());
                    UserBriefVO user = userMap.get(c.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getName());
                        vo.setAvatarUrl(user.getAvatarUrl());
                    }
                    return vo;
                }).collect(Collectors.groupingBy(SubmissionCommentVO::getParentId));

        List<SubmissionCommentVO> result = paged.stream().map(c -> {
            SubmissionCommentVO vo = new SubmissionCommentVO();
            vo.setId(c.getId());
            vo.setSubmissionId(c.getSubmissionId());
            vo.setParentId(c.getParentId());
            vo.setContent(c.getContent());
            vo.setUserId(c.getUserId());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            UserBriefVO user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setAvatarUrl(user.getAvatarUrl());
            }
            vo.setReplies(groupedReplies.getOrDefault(c.getId(), List.of()));
            return vo;
        }).toList();

        return new PageResponse<>(result, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    @Transactional
    public void updateComment(String commentId, String userId, SubmissionCommentDTO dto) {
        SubmissionComments comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Comment not found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You can only edit your own comments");
        }

        comment.setContent(dto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        boolean updated = this.updateById(comment);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update comment");
        }
    }

    @Override
    public long countComments(String submissionId) {
        if (StrUtil.isBlank(submissionId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Submission ID must not be blank");
        }

        return this.lambdaQuery()
                .eq(SubmissionComments::getSubmissionId, submissionId)
                .count();
    }

    @Override
    public Long countAllComments() {
        return this.lambdaQuery().count();
    }

}
