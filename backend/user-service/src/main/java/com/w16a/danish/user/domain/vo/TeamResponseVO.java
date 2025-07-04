package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


/**
 *
 * View Object for full response details of a team, including metadata and member list.
 *
 * @author Eddy ZHANG
 * @date 2025/04/16
 */
@Data
@Schema(description = "Full response details of a team including metadata and member list.")
public class TeamResponseVO {

    @Schema(description = "Team ID", example = "f327ef08-4b25-4a15-bf42-5e6f891b334b")
    private String id;

    @Schema(description = "Team name", example = "VisionAI")
    private String name;

    @Schema(description = "Team description", example = "A passionate team working on AI and robotics")
    private String description;

    @Schema(description = "Creator ID", example = "user-123")
    private String createdBy;

    @Schema(description = "Creation timestamp", example = "2025-04-16T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp", example = "2025-04-16T12:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Team member list (with role and user info)")
    private List<TeamMemberVO> members;
}
