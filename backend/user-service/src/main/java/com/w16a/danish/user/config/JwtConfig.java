package com.w16a.danish.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description JWT configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long expiration;

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

}
