package com.notificationservice.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterConfiguration {

    /**
     * Whether rate limiting is enabled
     */
    private boolean enabled = false;

    /**
     * Rate limit configuration for creating URL shorteners
     */
    private RateLimitSettings create = new RateLimitSettings(10, 1, 5);

    /**
     * Rate limit configuration for reading URL shorteners
     */
    private RateLimitSettings read = new RateLimitSettings(100, 1, 2);

    /**
     * Rate limit configuration for URL redirects
     */
    private RateLimitSettings redirect = new RateLimitSettings(500, 1, 1);

    /**
     * Rate limit configuration for analytics endpoints
     */
    private RateLimitSettings analytics = new RateLimitSettings(30, 1, 3);

    /**
     * Rate limit configuration for admin operations
     */
    private RateLimitSettings admin = new RateLimitSettings(20, 1, 5);

    // Cache for RateLimiterConfig objects to avoid recreation
    private final Map<String, RateLimiterConfig> configCache = new ConcurrentHashMap<>();

    @Bean("urlShortenerCreateRateLimiter")
    public RateLimiter urlShortenerCreateRateLimiter() {
        return createRateLimiter("urlShortenerCreate", create);
    }

    @Bean("urlShortenerReadRateLimiter")
    public RateLimiter urlShortenerReadRateLimiter() {
        return createRateLimiter("urlShortenerRead", read);
    }

    @Bean("urlShortenerRedirectRateLimiter")
    public RateLimiter urlShortenerRedirectRateLimiter() {
        return createRateLimiter("urlShortenerRedirect", redirect);
    }

    @Bean("urlShortenerAnalyticsRateLimiter")
    public RateLimiter urlShortenerAnalyticsRateLimiter() {
        return createRateLimiter("urlShortenerAnalytics", analytics);
    }

    @Bean("urlShortenerAdminRateLimiter")
    public RateLimiter urlShortenerAdminRateLimiter() {
        return createRateLimiter("urlShortenerAdmin", admin);
    }

    private RateLimiter createRateLimiter(String name, RateLimitSettings settings) {
        // Create a cache key based on name and settings
        String cacheKey = String.format("%s_%d_%d_%d",
                name,
                settings.getMaxRequests(),
                settings.getWindowMinutes(),
                settings.getTimeoutSeconds());

        // Get or create RateLimiterConfig from cache
        RateLimiterConfig rateLimiterConfig = configCache.computeIfAbsent(cacheKey,
                key -> RateLimiterConfig.custom()
                        .limitForPeriod(settings.getMaxRequests())
                        .limitRefreshPeriod(Duration.ofMinutes(settings.getWindowMinutes()))
                        .timeoutDuration(Duration.ofSeconds(settings.getTimeoutSeconds()))
                        .build());

        return RateLimiter.of(name, rateLimiterConfig);
    }

    @Data
    public static class RateLimitSettings {
        private int maxRequests;
        private int windowMinutes;
        private int timeoutSeconds;

        public RateLimitSettings() {
        }

        public RateLimitSettings(int maxRequests, int windowMinutes, int timeoutSeconds) {
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}