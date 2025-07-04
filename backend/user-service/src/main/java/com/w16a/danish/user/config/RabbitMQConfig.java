package com.w16a.danish.user.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration class for defining queues for user-service.
 * Including registration, submission, judge events, and award notifications.
 *
 * @author Eddy
 * @date 2025/04/13
 */
@Configuration
public class RabbitMQConfig {

    // === Registration/Submission Events ===
    public static final String REGISTER_SUCCESS_QUEUE = "register_success_queue";
    public static final String PARTICIPANT_REMOVED_QUEUE = "participant_removed_queue";
    public static final String SUBMISSION_UPLOADED_QUEUE = "submission_uploaded_queue";
    public static final String SUBMISSION_REVIEWED_QUEUE = "submission_reviewed_queue";

    // === Competition Judge Assignment Events ===
    public static final String JUDGE_ASSIGNED_QUEUE = "judge_assigned_queue";
    public static final String JUDGE_REMOVED_QUEUE = "judge_removed_queue";

    // === Judge Award Winner Events (New) ===
    public static final String AWARD_WINNER_QUEUE = "award_winner_queue";

    // === Queues ===
    @Bean
    public Queue registerSuccessQueue() {
        return QueueBuilder.durable(REGISTER_SUCCESS_QUEUE).build();
    }

    @Bean
    public Queue participantRemovedQueue() {
        return QueueBuilder.durable(PARTICIPANT_REMOVED_QUEUE).build();
    }

    @Bean
    public Queue submissionUploadedQueue() {
        return QueueBuilder.durable(SUBMISSION_UPLOADED_QUEUE).build();
    }

    @Bean
    public Queue submissionReviewedQueue() {
        return QueueBuilder.durable(SUBMISSION_REVIEWED_QUEUE).build();
    }

    @Bean
    public Queue judgeAssignedQueue() {
        return QueueBuilder.durable(JUDGE_ASSIGNED_QUEUE).build();
    }

    @Bean
    public Queue judgeRemovedQueue() {
        return QueueBuilder.durable(JUDGE_REMOVED_QUEUE).build();
    }

    @Bean
    public Queue awardWinnerQueue() {
        return QueueBuilder.durable(AWARD_WINNER_QUEUE).build();
    }

    // === Common JSON Converter and RabbitTemplate ===
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
