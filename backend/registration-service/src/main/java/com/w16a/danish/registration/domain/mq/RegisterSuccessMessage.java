package com.w16a.danish.registration.domain.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * This class represents a message sent to the message queue when a user successfully registers for a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Data
public class RegisterSuccessMessage implements Serializable {
    private String userName;
    private String userEmail;
    private String competitionName;
    private LocalDateTime registerTime;
}

