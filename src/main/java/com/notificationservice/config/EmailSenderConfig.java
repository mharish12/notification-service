package com.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "notification.email")
@Data
public class EmailSenderConfig {

    private Map<String, SenderConfig> senders;

    @Data
    public static class SenderConfig {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Map<String, String> properties;
    }
}