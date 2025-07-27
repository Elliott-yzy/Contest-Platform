package com.w16a.danish.judge.config;

import com.w16a.danish.judge.domain.mq.AwardWinnerMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MQ sender for notifying award winners.
 * Sends messages to judge.topic exchange with routing key award.winner.
 * (judge-service -> user-service or other downstreams)
 *
 * @author Eddy
 * @date 2025/04/19
 */
@Component
@RequiredArgsConstructor
public class AwardNotifier {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Send an award winner notification message.
     *
     * @param message Award winner information (personal or team award)
     */
    public void sendAwardWinner(AwardWinnerMessage message) {
        rabbitTemplate.convertAndSend(
                JudgeRabbitMQConfig.JUDGE_EXCHANGE_NAME,
                JudgeRabbitMQConfig.AWARD_WINNER_ROUTING_KEY,
                message,
                m -> {
                    m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return m;
                }
        );
    }
}
