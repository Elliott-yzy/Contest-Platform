package com.w16a.danish.judge.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing the mapping between judges and competitions.
 * Each judge is assigned to one or more competitions.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("competition_judges")
@Schema(name = "CompetitionJudges", description = "Mapping between competition and assigned judges")
public class CompetitionJudges implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition ID (UUID)", example = "comp-123e4567-e89b-12d3-a456-426614174000")
    private String competitionId;

    @Schema(description = "Judge User ID (UUID)", example = "user-123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "Record creation timestamp", example = "2025-04-18T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Record update timestamp", example = "2025-04-18T12:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
