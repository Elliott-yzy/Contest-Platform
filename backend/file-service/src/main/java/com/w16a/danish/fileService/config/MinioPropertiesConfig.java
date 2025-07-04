package com.w16a.danish.fileService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Minio Properties
 *
 * @author Eddy ZHANG
 * @date 2025/03/27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioPropertiesConfig {
    private String internalEndpoint;
    private String publicEndpoint;
    private String accessKey;
    private String secretKey;
}
