package com.w16a.danish.interaction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * This is the main class for the Interaction Service application.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.w16a.danish.interaction.mapper")
@EnableFeignClients(basePackages = "com.w16a.danish.interaction.feign")
public class InteractionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteractionServiceApplication.class, args);
    }
}
