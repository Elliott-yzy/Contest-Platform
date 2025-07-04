package com.w16a.danish.user.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Eddy ZHANG
 * @date 2025/04/16
 * @description Team Table (Stores team information)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team")
@Schema(name = "Team", description = "Table for team information")
public class Team implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Team ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Team Name", example = "AI Champions")
    private String name;

    @Schema(description = "Team Description", example = "We are passionate about machine learning and competitions", nullable = true)
    private String description;

    @Schema(description = "User ID of the team creator", example = "456e7890-e12d-34f5-a678-123456789abc")
    private String createdBy;

    @Schema(description = "Timestamp when the team was created", example = "2025-04-16T10:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the team was last updated", example = "2025-04-16T14:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
