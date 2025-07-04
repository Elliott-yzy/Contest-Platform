package com.w16a.danish.competition.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.w16a.danish.competition.domain.enums.ParticipationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for updating competition details.
 * All fields are optional for partial update.
 *
 * @author Eddy
 * @date 2025/04/01
 */
@Data
@Schema(name = "CompetitionUpdateDTO", description = "Request payload for updating a competition")
public class CompetitionUpdateDTO {

    @Schema(description = "Updated competition name", example = "New AI Challenge", nullable = true)
    private String name;

    @Schema(description = "Updated competition description", example = "A new exciting challenge.", nullable = true)
    private String description;

    @Schema(description = "Updated category", example = "Web, AI", nullable = true)
    private String category;

    @Schema(description = "New start time", example = "2025-05-01T10:00:00Z", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private LocalDateTime startDate;

    @Schema(description = "New end time", example = "2025-06-01T18:00:00Z", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private LocalDateTime endDate;

    @Schema(description = "Whether the competition is public", example = "false", nullable = true)
    private Boolean isPublic;

    @Schema(description = "Competition status", example = "UPCOMING")
    private String status;

    @Schema(description = "Updated submission types", example = "[\"ZIP\", \"PDF\"]", nullable = true)
    private List<String> allowedSubmissionTypes;

    @Schema(description = "Updated scoring criteria", example = "[\"Creativity\", \"Efficiency\"]", nullable = true)
    private List<String> scoringCriteria;

    @Schema(description = "Updated introduction video URL", example = "https://cdn.example.com/video.mp4", nullable = true)
    private String introVideoUrl;

    @Schema(description = "Updated image URLs for competition display", example = "[\"https://cdn.example.com/img1.jpg\"]", nullable = true)
    private List<String> imageUrls;

    @Schema(description = "Updated participation type (optional)", example = "TEAM")
    private ParticipationType participationType;

}
