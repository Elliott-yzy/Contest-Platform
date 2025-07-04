package com.w16a.danish.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description JWT configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    // JWT secret key
    private String secret;

    // JWT expiration time
    private long expiration;

    // JWT issuer
    private List<String> publicUrls;
}
