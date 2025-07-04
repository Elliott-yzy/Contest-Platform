package com.w16a.danish.judge.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * This class represents the information of a participant's submitted work in a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/05
 */
@Data
@Schema(name = "SubmissionInfoVO", description = "Participant's submitted work info")
public class SubmissionInfoVO {

    @Schema(description = "Submission ID", example = "abc-123")
    private String id;

    @Schema(description = "Competition ID", example = "comp-456")
    private String competitionId;

    @Schema(description = "Participant user ID", example = "user-789")
    private String userId;

    @Schema(description = "Submission title", example = "Awesome AI Bot")
    private String title;

    @Schema(description = "Submission description", example = "A fully working AI bot using LLMs")
    private String description;

    @Schema(description = "Uploaded file name", example = "aibot.zip")
    private String fileName;

    @Schema(description = "Uploaded file URL", example = "http://cdn.example.com/aibot.zip")
    private String fileUrl;

    @Schema(description = "File type", example = "ZIP")
    private String fileType;

    @Schema(description = "Review status", example = "PENDING / APPROVED / REJECTED")
    private String reviewStatus;

    @Schema(description = "Review comments", example = "Needs more testing coverage")
    private String reviewComments;

    @Schema(description = "Reviewer's user ID", example = "reviewer-001")
    private String reviewedBy;

    @Schema(description = "Review timestamp", example = "2025-04-05T14:30:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "Total score given", example = "87.50")
    private BigDecimal totalScore;

    @Schema(description = "Submission time", example = "2025-04-04T16:00:00")
    private LocalDateTime createdAt;
}
