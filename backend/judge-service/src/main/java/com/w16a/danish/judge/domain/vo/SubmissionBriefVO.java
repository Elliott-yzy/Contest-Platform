package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Represents a brief view object for a submission to assist judges during evaluation.
 * Displays basic info, file access, and scoring status.
 * Includes original file name for easier identification.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@Schema(name = "SubmissionBriefVO", description = "Brief info of a submission for judging")
public class SubmissionBriefVO {

    @Schema(description = "Submission ID", example = "subm-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Submission title", example = "Smart Recycling Robot")
    private String title;

    @Schema(description = "Short description of the submission", example = "An AI-based robot to sort recyclables.")
    private String description;

    @Schema(description = "Original file name of the uploaded submission", example = "robot_design.zip")
    private String fileName;

    @Schema(description = "URL to view or download the submitted file", example = "https://cdn.example.com/submissions/robot_design.zip")
    private String fileUrl;

    @Schema(description = "Whether the submission has already been scored by the current judge", example = "false")
    private Boolean hasScored;

    @Schema(description = "Last updated timestamp of the submission", example = "2025-06-01T18:00:00")
    private String lastUpdatedAt;
}
