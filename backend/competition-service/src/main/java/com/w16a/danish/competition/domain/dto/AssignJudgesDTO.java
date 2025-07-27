package com.w16a.danish.competition.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 *
 * DTO for assigning judges to a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/18
 */
@Data
@Schema(description = "DTO for assigning judges to a competition")
public class AssignJudgesDTO {

    @Schema(description = "List of judge emails to assign", example = "[\"judge1@example.com\", \"judge2@example.com\"]")
    private List<String> judgeEmails;
}
