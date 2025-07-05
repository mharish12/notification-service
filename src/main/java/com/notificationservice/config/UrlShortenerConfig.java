package com.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "url-shortener")
public class UrlShortenerConfig {

    /**
     * Base URL for short URLs (e.g., http://localhost:8080)
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * Short URL path prefix (e.g., /s)
     */
    private String shortUrlPath = "/s";

    /**
     * Default expiration days for URLs (null means no expiration)
     */
    private Integer defaultExpirationDays = null;

    /**
     * Maximum length for custom aliases
     */
    private Integer maxCustomAliasLength = 50;

    /**
     * Maximum length for short codes
     */
    private Integer maxShortCodeLength = 25;

    /**
     * Whether to enable password protection feature
     */
    private Boolean enablePasswordProtection = true;

    /**
     * Whether to track IP addresses and user agents
     */
    private Boolean enableTracking = true;

    /**
     * Cleanup expired URLs automatically (in hours)
     */
    private Integer cleanupIntervalHours = 24;

    /**
     * Get the full short URL for a given short code
     */
    public String getShortUrl(String shortCode) {
        return baseUrl + shortUrlPath + "/" + shortCode;
    }

    /**
     * Get the base URL with short path
     */
    public String getShortUrlBase() {
        return baseUrl + shortUrlPath;
    }
}