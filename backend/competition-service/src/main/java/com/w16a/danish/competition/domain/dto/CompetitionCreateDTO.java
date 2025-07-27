package com.w16a.danish.competition.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.w16a.danish.competition.domain.enums.ParticipationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description DTO for creating a new competition
 */
@Data
@Schema(name = "CompetitionCreateDTO", description = "Request payload for creating a new competition")
public class CompetitionCreateDTO {

    @Schema(description = "Competition name", example = "AI Coding Challenge")
    @NotBlank(message = "Competition name cannot be empty")
    private String name;

    @Schema(description = "Competition description", example = "An AI coding challenge for developers", nullable = true)
    private String description;

    @Schema(description = "Competition category", example = "AI/ML, Web Development", nullable = true)
    private String category;

    @Schema(description = "Competition start date", example = "2025-05-01T10:00:00Z")
    @NotNull(message = "Start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private LocalDateTime startDate;

    @Schema(description = "Competition end date", example = "2025-06-01T18:00:00Z")
    @NotNull(message = "End date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private LocalDateTime endDate;

    @Schema(description = "Whether the competition is public", example = "true", nullable = true)
    private Boolean isPublic = true;

    @Schema(description = "Competition status", example = "UPCOMING")
    private String status;

    @Schema(description = "Allowed submission types", example = "[\"PDF\", \"ZIP\", \"CODE\"]", nullable = true)
    private List<String> allowedSubmissionTypes;

    @Schema(description = "Scoring criteria", example = "[\"Innovation\", \"Performance\", \"Accuracy\"]", nullable = true)
    private List<String> scoringCriteria;

    @Schema(description = "URL for the competition introduction video", example = "https://example.com/video.mp4", nullable = true)
    private String introVideoUrl;

    @Schema(description = "List of image URLs for competition display", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]", nullable = true)
    private List<String> imageUrls;

    @Schema(description = "Participation type: INDIVIDUAL or TEAM", example = "INDIVIDUAL")
    private ParticipationType participationType = ParticipationType.INDIVIDUAL;

}
