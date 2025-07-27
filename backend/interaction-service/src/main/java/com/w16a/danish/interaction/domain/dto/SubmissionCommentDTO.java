package com.w16a.danish.interaction.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * This class represents a Data Transfer Object (DTO) for creating a comment or reply on a submission.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@Data
@Schema(description = "DTO for creating a submission comment or reply")
public class SubmissionCommentDTO {

    @Schema(description = "Submission ID being commented on", example = "abc123-submission-id", required = true)
    private String submissionId;

    @Schema(description = "Optional parent comment ID if this is a reply", example = "parent-comment-uuid")
    private String parentId;

    @Schema(description = "Content of the comment", example = "Awesome work! Could you share your model config?", required = true)
    private String content;
}
