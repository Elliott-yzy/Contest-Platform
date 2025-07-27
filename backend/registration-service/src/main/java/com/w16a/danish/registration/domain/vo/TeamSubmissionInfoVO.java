package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * View Object for displaying a team's submission info for a competition.
 *
 * @author Eddy
 * @date 2025/04/18
 */
@Data
@Schema(name = "TeamSubmissionInfoVO", description = "Submission details for a team in a competition")
public class TeamSubmissionInfoVO {

    @Schema(description = "Submission ID", example = "sub-123e4567-e89b-12d3-a456-426614174000")
    private String submissionId;

    @Schema(description = "Competition ID", example = "comp-123e4567-e89b-12d3-a456-426614174000")
    private String competitionId;

    @Schema(description = "Team ID", example = "team-123e4567-e89b-12d3-a456-426614174000")
    private String teamId;

    @Schema(description = "Title of the submission", example = "AI Traffic Prediction System")
    private String title;

    @Schema(description = "Description of the submission", example = "A system that uses machine learning to predict traffic congestion patterns.")
    private String description;

    @Schema(description = "Uploaded file name", example = "aibot.zip")
    private String fileName;

    @Schema(description = "URL to access the submitted file", example = "https://cdn.example.com/uploads/ai-traffic-system.pdf")
    private String fileUrl;

    @Schema(description = "File type", example = "ZIP")
    private String fileType;

    @Schema(description = "Time the submission was created", example = "2025-04-18T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Review status (PENDING/APPROVED/REJECTED)", example = "APPROVED")
    private String reviewStatus;

    @Schema(description = "Review comments", example = "Needs more testing coverage")
    private String reviewComments;

    @Schema(description = "Reviewer's user ID", example = "reviewer-001")
    private String reviewedBy;

    @Schema(description = "Review timestamp", example = "2025-04-05T14:30:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "Total score assigned after review (if any)", example = "92.5")
    private Double totalScore;
}
