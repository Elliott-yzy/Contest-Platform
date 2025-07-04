package com.w16a.danish.registration.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Entity representing the mapping between teams and competitions.
 * This table is used only for TEAM participation mode.
 *
 * @author Eddy
 * @date 2025/04/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("competition_teams")
@Schema(name = "CompetitionTeams", description = "Mapping between teams and competitions (used for team registration)")
public class CompetitionTeams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Mapping ID (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition ID (UUID)", example = "comp-123e4567-e89b-12d3-a456-426614174000")
    private String competitionId;

    @Schema(description = "Team ID (UUID)", example = "team-789e4567-e89b-12d3-a456-426614174000")
    private String teamId;

    @Schema(description = "Time when the team registered for the competition", example = "2025-04-17T10:30:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}
