package com.w16a.danish.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Represents a member within a team including user info and role.
 *
 * @author Eddy
 * @date 2025/04/16
 */
@Data
@Schema(description = "Team member detail")
public class TeamMemberVO {

    @Schema(description = "User ID of the member", example = "user-456")
    private String userId;

    @Schema(description = "User name", example = "Alice Chen")
    private String name;

    @Schema(description = "User email", example = "alice.chen@example.com")
    private String email;

    @Schema(description = "User avatar URL", example = "https://example.com/avatar/alice.jpg")
    private String avatarUrl;

    @Schema(description = "Short user description", example = "Frontend engineer and designer")
    private String description;

    @Schema(description = "Role within the team", example = "LEADER")
    private String role;
}
