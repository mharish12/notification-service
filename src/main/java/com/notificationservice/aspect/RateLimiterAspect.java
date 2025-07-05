package com.notificationservice.aspect;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.config.RateLimiterConfiguration;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private final RateLimiterConfiguration rateLimiterConfig;

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        if (!rateLimiterConfig.isEnabled()) {
            return joinPoint.proceed();
        }

        try {
            RateLimiter rateLimiter = getRateLimiter(rateLimited.type());

            return rateLimiter.executeSupplier(
                    () -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (RequestNotPermitted e) {
            log.warn("Rate limit exceeded for type: {}", rateLimited.type());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Rate limit exceeded",
                            "message", "Too many requests. Please try again later.",
                            "retryAfter", getRetryAfterSeconds(rateLimited.type())));
        } catch (Exception e) {
            log.error("Error in rate limiting for type: {}", rateLimited.type(), e);
            // In case of error, allow the request (fail-open)
            return joinPoint.proceed();
        }
    }

    private RateLimiter getRateLimiter(RateLimitType type) {
        return switch (type) {
            case CREATE -> rateLimiterConfig.urlShortenerCreateRateLimiter();
            case READ -> rateLimiterConfig.urlShortenerReadRateLimiter();
            case REDIRECT -> rateLimiterConfig.urlShortenerRedirectRateLimiter();
            case ANALYTICS -> rateLimiterConfig.urlShortenerAnalyticsRateLimiter();
            case ADMIN -> rateLimiterConfig.urlShortenerAdminRateLimiter();
        };
    }

    private int getRetryAfterSeconds(RateLimitType type) {
        return switch (type) {
            case CREATE -> 60; // 1 minute
            case READ -> 60; // 1 minute
            case REDIRECT -> 60; // 1 minute
            case ANALYTICS -> 60; // 1 minute
            case ADMIN -> 60; // 1 minute
        };
    }
}