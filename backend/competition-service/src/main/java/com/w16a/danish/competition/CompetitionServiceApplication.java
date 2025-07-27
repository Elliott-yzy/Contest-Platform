package com.w16a.danish.competition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description This is the main class of the competition service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.w16a.danish.competition.mapper")
@EnableFeignClients(basePackages = "com.w16a.danish.competition.feign")
public class CompetitionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompetitionServiceApplication.class, args);
    }
}
