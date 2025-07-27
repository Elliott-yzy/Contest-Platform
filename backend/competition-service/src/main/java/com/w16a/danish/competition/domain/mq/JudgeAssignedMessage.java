package com.w16a.danish.competition.domain.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 *
 * This class represents a message sent to the message queue when a judge is assigned to a competition.
 *
 * @author Eddy ZHANG
 * @date 2025/04/19
 */
@Data
public class JudgeAssignedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String judgeName;
    private String judgeEmail;
    private String competitionName;
    private LocalDateTime assignedAt;
}
