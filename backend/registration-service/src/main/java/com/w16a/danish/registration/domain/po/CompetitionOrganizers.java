package com.w16a.danish.registration.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Entity representing the organizer assigned to a competition.
 * Many-to-many mapping between users and competitions (organizer role).
 *
 * @author Eddy
 * @since 2025-04-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("competition_organizers")
@Schema(name = "CompetitionOrganizers", description = "Table for competition organizers (many-to-many relationship)")
public class CompetitionOrganizers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary key (UUID)", example = "8f2d3a60-1234-4c29-bc43-8d9e2184a50f")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "ID of the competition", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef")
    private String competitionId;

    @Schema(description = "User ID of the organizer", example = "user-uuid-123456")
    private String userId;

    @Schema(description = "Timestamp when the record was created", example = "2025-04-03T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-04-03T13:45:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
