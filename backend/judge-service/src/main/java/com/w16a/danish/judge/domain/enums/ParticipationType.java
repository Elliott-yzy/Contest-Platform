package com.w16a.danish.judge.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 *
 * This enum represents the participation type of a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/16
 */
@Getter
@Schema(description = "Participation type of the competition")
public enum ParticipationType {

    INDIVIDUAL("INDIVIDUAL", "Individual participants only"),
    TEAM("TEAM", "Team participants only");

    @EnumValue
    private final String value;

    private final String description;

    ParticipationType(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
