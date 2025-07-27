package com.w16a.danish.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Frontend URL builder for generating links in email notifications.
 * Supports reset password, email verification, OAuth, competition access, and judge competition list.
 *
 * @author Eddy
 * @date 2025/04/19
 */
@Configuration
@ConfigurationProperties(prefix = "frontend")
@Data
public class FrontendProperties {

    private String baseUrl;
    private String resetPasswordPath;
    private String emailVerificationPath;
    private String oauthCallbackPath;
    private String competitionPath;
    private String competitionJudgePath;

    /**
     * Build reset password URL.
     */
    public String buildResetPasswordUrl(String token) {
        return baseUrl + resetPasswordPath + "?token=" + encode(token);
    }

    /**
     * Build competition page URL for participants.
     */
    public String buildCompetitionPageUrl(String email) {
        return baseUrl + competitionPath + "/" + encode(email);
    }

    /**
     * Build competition judging page URL for judges.
     */
    public String buildJudgeCompetitionPageUrl(String email) {
        return baseUrl + competitionJudgePath + "/" + encode(email);
    }

    /**
     * Build email verification URL.
     */
    public String buildEmailVerificationUrl(String token) {
        return baseUrl + emailVerificationPath + "?token=" + encode(token);
    }

    /**
     * Build OAuth callback redirection URL.
     */
    public String buildOauthRedirectUrl(String token, String email, String role, String userId) {
        return String.format("%s%s?token=%s&email=%s&role=%s&userId=%s",
                baseUrl,
                oauthCallbackPath,
                encode(token),
                encode(email),
                encode(role),
                encode(userId));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
