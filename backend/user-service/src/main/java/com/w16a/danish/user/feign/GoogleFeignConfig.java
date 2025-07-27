package com.w16a.danish.user.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Google-Feign Config
 */
@Configuration
public class GoogleFeignConfig {
    @Bean
    public RequestInterceptor googleInterceptor() {
        return template -> template.header("Accept", "application/json");
    }
}
