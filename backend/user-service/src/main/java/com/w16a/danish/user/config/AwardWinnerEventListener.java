package com.w16a.danish.user.config;

import com.w16a.danish.user.domain.mq.AwardWinnerMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Listens to award winner events and sends email notifications.
 * (Triggered by messages from judge-service)
 *
 * @author Eddy
 * @date 2025/04/20
 */
@Component
@RequiredArgsConstructor
public class AwardWinnerEventListener {

    private final EmailService emailService;
    private final FrontendProperties frontendProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @RabbitListener(queues = RabbitMQConfig.AWARD_WINNER_QUEUE)
    public void handleAwardWinner(AwardWinnerMessage message) {
        String subject;
        String content;

        boolean hasAward = message.getAwardName() != null && !"None".equalsIgnoreCase(message.getAwardName());

        if (!hasAward) {
            subject = "Thank You for Participating – " + message.getCompetitionName();
            content = String.format("""
                    <div style="font-family:Arial,sans-serif;line-height:1.6;color:#333;">
                      <p>Hi <b>%s</b>,</p>

                      <p>Thank you for participating in <b>%s</b>! Although you didn't win this time, your contribution was greatly valued.</p>

                      <p>We encourage you to keep innovating and participating in future competitions!</p>

                      <hr>

                      <p><b>📅 Result Announced At:</b> %s</p>

                      <p>👉 Visit our platform for more opportunities: <a href="%s" style="color:#1a73e8;text-decoration:none;">Explore Competitions</a></p>

                      <hr>

                      <p style="font-size:12px;color:gray;">
                        This is an automated message from the <b>Danish Competition Platform</b>.
                      </p>
                    </div>
                    """,
                    message.getUserName(),
                    message.getCompetitionName(),
                    message.getAwardedAt().format(FORMATTER),
                    frontendProperties.getBaseUrl()
            );
        } else {
            subject = "🏆 Congratulations! You Won – " + message.getCompetitionName();
            content = String.format("""
                    <div style="font-family:Arial,sans-serif;line-height:1.6;color:#333;">
                      <p>Hi <b>%s</b>,</p>

                      <p>Congratulations! You have won the following award(s) in <b>%s</b>:</p>

                      <p><b>🏅 Awards:</b> %s</p>

                      <hr>

                      <p><b>📅 Result Announced At:</b> %s</p>

                      <p>👉 Explore more competitions: <a href="%s" style="color:#1a73e8;text-decoration:none;">Click to view</a></p>

                      <hr>

                      <p style="font-size:12px;color:gray;">
                        This is an automated message from the <b>Danish Competition Platform</b>.
                      </p>
                    </div>
                    """,
                    message.getUserName(),
                    message.getCompetitionName(),
                    message.getAwardName(),
                    message.getAwardedAt().format(FORMATTER),
                    frontendProperties.getBaseUrl()
            );
        }

        emailService.send(message.getUserEmail(), subject, content);
    }
}
