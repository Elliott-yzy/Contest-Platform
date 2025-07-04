package com.w16a.danish.competition.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.w16a.danish.competition.domain.enums.CompetitionStatus;
import com.w16a.danish.competition.domain.enums.ParticipationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description Competitions table
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "competitions", autoResultMap = true)
@Schema(name = "Competitions", description = "Competition entity representing a contest.")
public class Competitions implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary key (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition name", example = "AI Coding Challenge")
    @NotBlank(message = "Competition name cannot be empty")
    private String name;

    @Schema(description = "Competition description", example = "An AI coding challenge for developers.", nullable = true)
    private String description;

    @Schema(description = "Competition category", example = "AI/ML, Web Development", nullable = true)
    private String category;

    @Schema(description = "Competition start date", example = "2025-05-01T10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Competition start date cannot be empty")
    @TableField("start_date")
    private LocalDateTime startDate;

    @Schema(description = "Competition end date", example = "2025-06-01T18:00:00")
    @NotNull(message = "Competition end date cannot be empty")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @TableField("end_date")
    private LocalDateTime endDate;

    @Schema(description = "Whether the competition is public", example = "true", nullable = true)
    private Boolean isPublic;

    @Schema(description = "Competition status", example = "UPCOMING")
    @TableField(value = "status")
    private CompetitionStatus status;

    @Schema(description = "Allowed submission types", example = "[\"PDF\", \"ZIP\", \"CODE\"]", nullable = true)
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allowedSubmissionTypes;

    @Schema(description = "Scoring criteria", example = "[\"Innovation\", \"Performance\", \"Accuracy\"]", nullable = true)
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scoringCriteria;

    @Schema(description = "URL for the competition introduction video", example = "https://example.com/video.mp4", nullable = true)
    private String introVideoUrl;

    @Schema(description = "List of image URLs for competition display", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]", nullable = true)
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;

    @Schema(description = "Participation type (INDIVIDUAL or TEAM)", example = "INDIVIDUAL")
    @TableField("participation_type")
    private ParticipationType participationType = ParticipationType.INDIVIDUAL;

    @Schema(description = "Timestamp when the competition record was created", example = "2025-03-18T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-03-19T14:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


}
