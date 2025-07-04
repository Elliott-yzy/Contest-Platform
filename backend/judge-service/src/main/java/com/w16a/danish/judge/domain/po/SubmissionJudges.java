package com.w16a.danish.judge.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a judge's evaluation record for a submission.
 * Each judge independently scores and comments on submissions.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_judges")
@Schema(name = "SubmissionJudges", description = "Judge evaluation records for submissions")
public class SubmissionJudges implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key (UUID) for the judge evaluation record", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Submission ID being evaluated", example = "subm-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Competition ID associated with the submission", example = "comp-123e4567-e89b-12d3-a456-426614174000")
    private String competitionId;

    @Schema(description = "Judge user ID (UUID)", example = "user-123e4567-e89b-12d3-a456-426614174000")
    private String judgeId;

    @Schema(description = "Total score given by the judge", example = "85.5")
    private BigDecimal totalScore;

    @Schema(description = "General comments from the judge", example = "Great innovation and teamwork!")
    private String judgeComments;

    @Schema(description = "Record creation timestamp", example = "2025-04-18T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-04-18T12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
