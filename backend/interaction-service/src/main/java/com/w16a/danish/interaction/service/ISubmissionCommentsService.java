package com.w16a.danish.interaction.service;

import com.w16a.danish.interaction.domain.dto.SubmissionCommentDTO;
import com.w16a.danish.interaction.domain.po.SubmissionComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.interaction.domain.vo.PageResponse;
import com.w16a.danish.interaction.domain.vo.SubmissionCommentVO;

/**
 *
 * SubmissionCommentsService interface for handling submission comments.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
public interface ISubmissionCommentsService extends IService<SubmissionComments> {

    void addComment(String userId, SubmissionCommentDTO dto);

    void deleteComment(String commentId, String userId, String userRole);

    PageResponse<SubmissionCommentVO> getPaginatedComments(String submissionId, int page, int size, String sortBy, String order);

    void updateComment(String commentId, String userId, SubmissionCommentDTO dto);

    long countComments(String submissionId);

    Long countAllComments();

}
