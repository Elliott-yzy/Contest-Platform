package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Eddy
 * @date 2025/04/04
 * @description Universal user brief info used in competition-related views (participant/organizer/judge).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserBriefVO", description = "Basic user information for reference")
public class UserBriefVO {

    @Schema(description = "User ID", example = "a7b3c5e7-d8fa-4d02-98b1-23cfaa72f456")
    private String id;

    @Schema(description = "User name", example = "Alice Zhang")
    private String name;

    @Schema(description = "User email", example = "alice@example.com")
    private String email;

    @Schema(description = "Avatar URL", example = "https://cdn.example.com/avatar/alice.jpg")
    private String avatarUrl;

    @Schema(description = "User description or bio", example = "I am a passionate backend developer")
    private String description;

    @Schema(description = "User created/joined time", example = "2025-04-04T14:23:00")
    private LocalDateTime createdAt;
}
