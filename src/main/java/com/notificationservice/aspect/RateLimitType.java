package com.notificationservice.aspect;

public enum RateLimitType {
    CREATE, // For creating URL shorteners
    READ, // For reading URL shorteners
    REDIRECT, // For URL redirects
    ANALYTICS, // For analytics endpoints
    ADMIN // For admin operations
}