package com.w16a.danish.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 *
 * SubmissionComments domain object representing comments on submissions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_comments")
@Schema(name = "SubmissionComments", description = "Comments with optional parent for nested replies")
public class SubmissionComments implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Comment ID (UUID)", example = "cmt-123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "ID of the submission being commented on", example = "sub-abc-456")
    private String submissionId;

    @Schema(description = "User ID of the commenter", example = "user-uuid-456")
    private String userId;

    @Schema(description = "Parent comment ID (null if top-level comment)", example = "parent-uuid-789")
    private String parentId;

    @Schema(description = "Comment content", example = "Great idea, love the UI design!")
    private String content;

    @Schema(description = "Timestamp of comment creation", example = "2025-04-08T09:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2025-04-08T10:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
