package com.w16a.danish.registration.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description Competition status enum
 */
@Getter
@Schema(name = "CompetitionStatus", description = "Enum representing the status of a competition")
public enum CompetitionStatus {

    @Schema(description = "Upcoming competition", example = "UPCOMING")
    UPCOMING("UPCOMING"),

    @Schema(description = "Competition currently ongoing", example = "ONGOING")
    ONGOING("ONGOING"),

    @Schema(description = "Competition completed", example = "COMPLETED")
    COMPLETED("COMPLETED"),

    @Schema(description = "Competition awarded (winners announced)", example = "AWARDED")
    AWARDED("AWARDED"),

    @Schema(description = "Competition canceled", example = "CANCELED")
    CANCELED("CANCELED");

    private final String value;

    CompetitionStatus(String value) {
        this.value = value;
    }

    public static boolean isRegistrable(CompetitionStatus status) {
        return status == UPCOMING || status == ONGOING;
    }

    /**
     * Check if a competition is open for submission.
     * Only ONGOING competitions allow submission of work.
     */
    public static boolean isSubmittable(CompetitionStatus status) {
        return status == ONGOING;
    }

}
