package com.w16a.danish.judge.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing awarded submissions for a competition.
 * Records details such as award name, rank, and description.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_winners")
@Schema(name = "SubmissionWinners", description = "Entity for managing competition award winners")
public class SubmissionWinners implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key (UUID)", example = "win-123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition ID", example = "comp-456")
    private String competitionId;

    @Schema(description = "Submission ID (the work being awarded)", example = "subm-789")
    private String submissionId;

    @Schema(description = "Award name (e.g., Champion, Best Innovation)", example = "Champion")
    private String awardName;

    @Schema(description = "Submission ranking (1 = Champion, 2 = Runner-up, etc.)", example = "1")
    private Integer rankSubmission;

    @Schema(description = "Detailed description of the award (optional)", example = "Awarded for outstanding innovation and execution.")
    private String awardDescription;

    @Schema(description = "Record creation timestamp", example = "2025-04-18T15:30:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Record update timestamp", example = "2025-04-18T16:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
