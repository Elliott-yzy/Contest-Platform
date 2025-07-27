package com.w16a.danish.judge.domain.mq;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * AwardWinnerMessage is a message object that contains information about the award winner.
 *
 * @author Eddy ZHANG
 * @date 2025/04/19
 */
@Data
public class AwardWinnerMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userName;
    private String userEmail;
    private String competitionName;
    private String awardName;
    private LocalDateTime awardedAt;
}
