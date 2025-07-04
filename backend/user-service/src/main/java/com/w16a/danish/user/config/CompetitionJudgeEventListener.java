package com.w16a.danish.user.config;

import com.w16a.danish.user.domain.mq.JudgeAssignedMessage;
import com.w16a.danish.user.domain.mq.JudgeRemovedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Listens to competition judge assignment/removal events and sends email notifications.
 * (Triggered by messages from competition-service)
 *
 * @author Eddy
 * @date 2025/04/19
 */
@Component
@RequiredArgsConstructor
public class CompetitionJudgeEventListener {

    private final EmailService emailService;
    private final FrontendProperties frontendProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Handle judge assigned event.
     */
    @RabbitListener(queues = RabbitMQConfig.JUDGE_ASSIGNED_QUEUE)
    public void handleJudgeAssigned(JudgeAssignedMessage message) {
        String subject = "🎖️ Judge Assignment Notification – " + message.getCompetitionName();
        String judgeCompetitionUrl = frontendProperties.buildJudgeCompetitionPageUrl(message.getJudgeEmail());

        String content = String.format("""
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#333;">
                  <p>Hi <b>%s</b>,</p>

                  <p>Congratulations! You have been assigned as a <b>Judge</b> for the competition <b>%s</b>.</p>

                  <hr>

                  <p><b>📅 Assigned at:</b> %s</p>

                  <p>You can view your assigned competitions here:<br>
                  👉 <a href="%s" style="color:#1a73e8;text-decoration:none;">Click to view your judging competitions</a></p>

                  <hr>

                  <p style="font-size:12px;color:gray;">
                    This is an automated message from the <b>Danish Competition Platform</b>.
                  </p>
                </div>
                """,
                message.getJudgeName(),
                message.getCompetitionName(),
                message.getAssignedAt().format(FORMATTER),
                judgeCompetitionUrl
        );

        emailService.send(message.getJudgeEmail(), subject, content);
    }

    /**
     * Handle judge removed event.
     */
    @RabbitListener(queues = RabbitMQConfig.JUDGE_REMOVED_QUEUE)
    public void handleJudgeRemoved(JudgeRemovedMessage message) {
        String subject = "❌ Judge Removal Notification – " + message.getCompetitionName();
        String judgeCompetitionUrl = frontendProperties.buildJudgeCompetitionPageUrl(message.getJudgeEmail());

        String content = String.format("""
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#333;">
                  <p>Hi <b>%s</b>,</p>

                  <p>You have been <b>removed</b> from the Judges list for the competition <b>%s</b>.</p>

                  <hr>

                  <p><b>📅 Removed at:</b> %s</p>

                  <p>You can view your other assigned competitions here:<br>
                  👉 <a href="%s" style="color:#1a73e8;text-decoration:none;">Click to view your judging competitions</a></p>

                  <hr>

                  <p style="font-size:12px;color:gray;">
                    This is an automated message from the <b>Danish Competition Platform</b>.
                  </p>
                </div>
                """,
                message.getJudgeName(),
                message.getCompetitionName(),
                message.getRemovedAt().format(FORMATTER),
                judgeCompetitionUrl
        );

        emailService.send(message.getJudgeEmail(), subject, content);
    }
}
