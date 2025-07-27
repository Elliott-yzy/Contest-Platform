package com.w16a.danish.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Google OAuth2 properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "google")
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizeUrl;
    private String tokenUrl;
    private String userInfoUrl;
}
