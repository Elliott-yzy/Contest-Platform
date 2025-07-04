package com.w16a.danish.user.domain.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * This class represents a message indicating that a submission has been reviewed.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Data
public class SubmissionReviewedMessage implements Serializable {
    private String userName;
    private String userEmail;
    private String competitionName;
    private String title;
    private String reviewStatus;
    private String reviewedBy;
    private String reviewComments;
    private LocalDateTime reviewedAt;
}
