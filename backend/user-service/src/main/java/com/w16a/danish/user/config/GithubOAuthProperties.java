package com.w16a.danish.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Github-OAuth properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "github")
public class GithubOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String authorizeUrl;
    private String redirectUri;

}