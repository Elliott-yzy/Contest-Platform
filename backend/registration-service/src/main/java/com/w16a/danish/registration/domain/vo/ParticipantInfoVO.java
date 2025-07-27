package com.w16a.danish.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * View Object representing participant info in a competition.
 * Used for organizers to view who has registered.
 *
 * @author Eddy
 * @since 2025/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ParticipantInfoVO", description = "Participant detail info in a competition")
public class ParticipantInfoVO {

    @Schema(description = "User ID", example = "a7b3c5e7-d8fa-4d02-98b1-23cfaa72f456")
    private String userId;

    @Schema(description = "User name", example = "Alice Zhang")
    private String name;

    @Schema(description = "User email", example = "alice@example.com")
    private String email;

    @Schema(description = "Avatar URL", example = "https://cdn.example.com/avatar/alice.jpg")
    private String avatarUrl;

    @Schema(description = "User description or bio", example = "I am a passionate backend developer")
    private String description;

    @Schema(description = "Time the user registered for this competition", example = "2025-04-04T14:23:00")
    private LocalDateTime registeredAt;
}