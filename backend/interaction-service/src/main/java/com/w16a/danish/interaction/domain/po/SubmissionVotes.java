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
 * SubmissionVotes
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_votes")
@Schema(name = "SubmissionVotes", description = "Votes on submissions")
public class SubmissionVotes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Vote ID (UUID)", example = "vote-123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "ID of the voted submission", example = "subm-abc-1234")
    private String submissionId;

    @Schema(description = "User ID who voted", example = "user-uuid-456")
    private String userId;

    @Schema(description = "Vote timestamp", example = "2025-04-08T11:45:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
