package com.w16a.danish.competition.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Time Zone Configuration
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Australia/Sydney")));
    }
}
