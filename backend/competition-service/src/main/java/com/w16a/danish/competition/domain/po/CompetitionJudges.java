package com.w16a.danish.competition.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description Table for competition judges (many-to-many relationship)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("competition_judges")
@Schema(name = "CompetitionJudges", description = "Table for mapping judges to competitions (many-to-many relationship)")
public class CompetitionJudges implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary key (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Competition ID (UUID)", example = "abc123-def456-ghi789-jkl012")
    private String competitionId;

    @Schema(description = "Judge user ID (UUID)", example = "xyz123-lmn456-opq789-rst012")
    private String userId;

    @Schema(description = "Timestamp when the judge was assigned to the competition", example = "2025-03-18T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-03-19T14:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
