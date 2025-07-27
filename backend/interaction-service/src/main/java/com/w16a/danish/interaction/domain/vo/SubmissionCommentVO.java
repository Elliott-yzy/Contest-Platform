package com.w16a.danish.interaction.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * This class represents a comment on a submission, including optional nested replies.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@Data
@Schema(description = "VO for submission comments with optional nested replies")
public class SubmissionCommentVO {

    @Schema(description = "Comment ID", example = "cmt-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Submission ID", example = "subm-456e7890-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Parent comment ID (null if top-level)", example = "parent-comment-id")
    private String parentId;

    @Schema(description = "Comment content", example = "Great submission, well structured!")
    private String content;

    @Schema(description = "User ID of the commenter", example = "user-123")
    private String userId;

    @Schema(description = "Commenter's display name", example = "Alice")
    private String userName;

    @Schema(description = "Commenter's avatar URL", example = "https://cdn.example.com/avatar/alice.png")
    private String avatarUrl;

    @Schema(description = "Comment created time", example = "2025-04-08T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated time", example = "2025-04-08T15:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "List of child comments (replies)")
    private List<SubmissionCommentVO> replies;
}
