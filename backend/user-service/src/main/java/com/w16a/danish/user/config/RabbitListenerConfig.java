package com.w16a.danish.user.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * This class configures the RabbitMQ message listener container factory.
 *
 * @author Eddy ZHANG
 * @date 2025/04/13
 */
@Configuration
@EnableRabbit
public class RabbitListenerConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
