package com.w16a.danish.judge.domain.vo;

import com.w16a.danish.judge.domain.enums.CompetitionStatus;
import com.w16a.danish.judge.domain.enums.ParticipationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description Response object for competition creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CompetitionResponseVO", description = "Response object for competition creation")
public class CompetitionResponseVO {

    @Schema(description = "Competition ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Competition name", example = "AI Coding Challenge")
    private String name;

    @Schema(description = "Competition description", example = "An AI coding challenge for developers.")
    private String description;

    @Schema(description = "Competition category", example = "AI/ML, Web Development")
    private String category;

    @Schema(description = "Competition start date", example = "2025-05-01T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Competition end date", example = "2025-06-01T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Whether the competition is public", example = "true")
    private Boolean isPublic;

    @Schema(description = "Competition status", example = "UPCOMING")
    private CompetitionStatus status;

    @Schema(description = "Allowed submission types", example = "[\"PDF\", \"ZIP\", \"CODE\"]")
    private List<String> allowedSubmissionTypes;

    @Schema(description = "Scoring criteria", example = "[\"Innovation\", \"Performance\", \"Accuracy\"]")
    private List<String> scoringCriteria;

    @Schema(description = "URL for the competition introduction video", example = "https://example.com/video.mp4", nullable = true)
    private String introVideoUrl;

    @Schema(description = "List of image URLs for competition display", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]", nullable = true)
    private List<String> imageUrls;

    @Schema(description = "Participation type of the competition", example = "TEAM")
    private ParticipationType participationType;

    @Schema(description = "Competition creation timestamp", example = "2025-03-18T12:00:00")
    private LocalDateTime createdAt;
}
