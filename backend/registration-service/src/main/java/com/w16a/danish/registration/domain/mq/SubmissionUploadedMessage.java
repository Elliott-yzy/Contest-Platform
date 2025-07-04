package com.w16a.danish.registration.domain.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * This class represents a message that is sent when a submission is uploaded.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Data
public class SubmissionUploadedMessage implements Serializable {
    private String userName;
    private String userEmail;
    private String competitionName;
    private String title;
    private LocalDateTime submittedAt;
}
