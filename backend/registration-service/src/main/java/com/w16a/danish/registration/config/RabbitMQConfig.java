package com.w16a.danish.registration.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * This class configures RabbitMQ for the registration service.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "registration.topic";

    // Queue Names
    public static final String REGISTER_SUCCESS_QUEUE = "register_success_queue";
    public static final String PARTICIPANT_REMOVED_QUEUE = "participant_removed_queue";
    public static final String SUBMISSION_UPLOADED_QUEUE = "submission_uploaded_queue";
    public static final String SUBMISSION_REVIEWED_QUEUE = "submission_reviewed_queue";

    // Routing Keys
    public static final String REGISTER_SUCCESS_ROUTING_KEY = "register.success";
    public static final String PARTICIPANT_REMOVED_ROUTING_KEY = "register.removed";
    public static final String SUBMISSION_UPLOADED_ROUTING_KEY = "submission.uploaded";
    public static final String SUBMISSION_REVIEWED_ROUTING_KEY = "submission.reviewed";

    // Exchange
    @Bean
    public TopicExchange registrationExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    // Queues
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

    // Bindings
    @Bean
    public Binding registerSuccessBinding() {
        return BindingBuilder.bind(registerSuccessQueue())
                .to(registrationExchange()).with(REGISTER_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding participantRemovedBinding() {
        return BindingBuilder.bind(participantRemovedQueue())
                .to(registrationExchange()).with(PARTICIPANT_REMOVED_ROUTING_KEY);
    }

    @Bean
    public Binding submissionUploadedBinding() {
        return BindingBuilder.bind(submissionUploadedQueue())
                .to(registrationExchange()).with(SUBMISSION_UPLOADED_ROUTING_KEY);
    }

    @Bean
    public Binding submissionReviewedBinding() {
        return BindingBuilder.bind(submissionReviewedQueue())
                .to(registrationExchange()).with(SUBMISSION_REVIEWED_ROUTING_KEY);
    }

    // JSON message converter
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
