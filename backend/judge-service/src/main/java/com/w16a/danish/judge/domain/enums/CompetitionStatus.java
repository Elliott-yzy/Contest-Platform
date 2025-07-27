package com.w16a.danish.judge.domain.enums;

import com.w16a.danish.judge.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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

    public static CompetitionStatus fromString(String statusString) {
        for (CompetitionStatus status : CompetitionStatus.values()) {
            if (status.value.equalsIgnoreCase(statusString)) {
                return status;
            }
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid competition status: " + statusString);
    }
}

