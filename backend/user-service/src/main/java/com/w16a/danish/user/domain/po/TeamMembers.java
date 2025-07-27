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
 * @description Mapping table between users and teams
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("team_members")
@Schema(name = "TeamMembers", description = "Mapping table between users and teams")
public class TeamMembers implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Team Member Mapping ID (UUID)", example = "abc123-def456-ghi789")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "Team ID", example = "team-001-uuid")
    private String teamId;

    @Schema(description = "User ID", example = "user-001-uuid")
    private String userId;

    @Schema(description = "Role within the team (e.g., Leader, Designer, Developer)", example = "Leader")
    private String role;

    @Schema(description = "Join Timestamp", example = "2025-04-16T10:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;
}
