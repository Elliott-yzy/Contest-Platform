package com.w16a.danish.judge.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ in judge-service.
 * Used for notifying award winners, etc.
 *
 * @author Eddy
 * @date 2025/04/19
 */
@Configuration
public class JudgeRabbitMQConfig {

    // Exchange Name (for all judge-service events)
    public static final String JUDGE_EXCHANGE_NAME = "judge.topic";

    // Queue Names
    public static final String AWARD_WINNER_QUEUE = "award_winner_queue";

    // Routing Keys
    public static final String AWARD_WINNER_ROUTING_KEY = "award.winner";

    /**
     * Define topic exchange for judge service
     */
    @Bean
    public TopicExchange judgeExchange() {
        return ExchangeBuilder.topicExchange(JUDGE_EXCHANGE_NAME)
                .durable(true)
                .build();
    }

    /**
     * Queue for notifying award winners
     */
    @Bean
    public Queue awardWinnerQueue() {
        return QueueBuilder.durable(AWARD_WINNER_QUEUE)
                .build();
    }

    /**
     * Binding between award winner queue and exchange
     */
    @Bean
    public Binding awardWinnerBinding() {
        return BindingBuilder.bind(awardWinnerQueue())
                .to(judgeExchange())
                .with(AWARD_WINNER_ROUTING_KEY);
    }

    /**
     * Message converter for JSON format
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
