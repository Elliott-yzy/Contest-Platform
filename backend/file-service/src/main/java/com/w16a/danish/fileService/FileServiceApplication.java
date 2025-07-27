package com.w16a.danish.fileService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *
 * File Service Application
 *
 * @author Eddy ZHANG
 * @date 2025/03/27
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
