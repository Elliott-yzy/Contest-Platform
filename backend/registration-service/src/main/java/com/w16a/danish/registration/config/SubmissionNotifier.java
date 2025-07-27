package com.w16a.danish.registration.config;

import com.w16a.danish.registration.domain.mq.SubmissionReviewedMessage;
import com.w16a.danish.registration.domain.mq.SubmissionUploadedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * This class is responsible for sending messages related to submission events.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Component
@RequiredArgsConstructor
public class SubmissionNotifier {

    private final RabbitTemplate rabbitTemplate;

    public void sendSubmissionUploaded(SubmissionUploadedMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.SUBMISSION_UPLOADED_ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                }
        );
    }

    public void sendSubmissionReviewed(SubmissionReviewedMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.SUBMISSION_REVIEWED_ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                }
        );
    }

}

