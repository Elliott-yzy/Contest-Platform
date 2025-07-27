package com.w16a.danish.judge.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity representing detailed criterion scores assigned by a judge for a submission.
 * Each criterion is scored individually with its respective weight.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_judge_scores")
@Schema(name = "SubmissionJudgeScores", description = "Individual criterion scores assigned by judges")
public class SubmissionJudgeScores implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key (UUID) for the judge score record", example = "score-123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "ID linking to the judge's evaluation record", example = "judge-123e4567-e89b-12d3-a456-426614174000")
    private String judgeRecordId;

    @Schema(description = "ID of the submission", example = "submission-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Name of the scoring criterion", example = "Innovation")
    private String criterion;

    @Schema(description = "Score given for the criterion", example = "8.5")
    private BigDecimal score;

    @Schema(description = "Weight assigned to this criterion in overall score", example = "0.4")
    private BigDecimal weight;

}
