package com.w16a.danish.user.domain.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * This class represents a message that is sent when a participant is removed from a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Data
public class ParticipantRemovedMessage implements Serializable {
    private String userName;
    private String userEmail;
    private String removedBy;
    private String competitionName;
    private LocalDateTime removedAt;
}