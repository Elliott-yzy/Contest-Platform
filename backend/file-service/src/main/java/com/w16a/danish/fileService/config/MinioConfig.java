package com.w16a.danish.fileService.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Minio Configuration Class
 *
 * @author Eddy ZHANG
 * @date 2025/03/27
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioPropertiesConfig minioPropertiesConfig;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioPropertiesConfig.getInternalEndpoint())
                .credentials(minioPropertiesConfig.getAccessKey(), minioPropertiesConfig.getSecretKey())
                .build();
    }
}

