package com.w16a.danish.registration.domain.po;

import java.io.Serial;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Eddy ZHANG
 * @date 2025/04/03
 * @description Submission records by participants for competitions
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("submission_records")
@Schema(name = "SubmissionRecords", description = "Submission records by participants for competitions")
public class SubmissionRecords implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Submission record ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition ID", example = "comp-1234-uuid")
    private String competitionId;

    @Schema(description = "User ID of the participant", example = "user-uuid-456")
    private String userId;

    @Schema(description = "Optional Team ID if submitted as team", example = "team-uuid-1234")
    private String teamId;

    @Schema(description = "Title of the submission", example = "AI Robot Project")
    private String title;

    @Schema(description = "Optional description of the submission", example = "This project uses GPT model to...")
    private String description;

    @Schema(description = "Original file name", example = "submission.zip")
    private String fileName;

    @Schema(description = "MinIO or storage URL to the submitted file", example = "https://cdn.example.com/files/abc123.zip")
    private String fileUrl;

    @Schema(description = "Type of the file (PDF, ZIP, CODE, etc.)", example = "ZIP")
    private String fileType;

    @Schema(description = "Review status (e.g. PENDING, APPROVED, REJECTED)", example = "PENDING")
    private String reviewStatus;

    @Schema(description = "Optional comments from reviewers", example = "Well structured, but needs more documentation.")
    private String reviewComments;

    @Schema(description = "User ID of the reviewer", example = "admin-uuid-999")
    private String reviewedBy;

    @Schema(description = "Time when the submission was reviewed", example = "2025-04-05T15:30:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "Total score given to the submission", example = "87.50")
    private BigDecimal totalScore;

    @Schema(description = "Timestamp of record creation", example = "2025-04-03T10:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of last update", example = "2025-04-04T14:15:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
