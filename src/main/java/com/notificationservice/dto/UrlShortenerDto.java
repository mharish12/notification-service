package com.notificationservice.dto;

import com.notificationservice.dto.BaseAuditableDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UrlShortenerDto extends BaseAuditableDto {

    private Long id;

    @NotBlank(message = "Original URL is required")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", message = "Please provide a valid URL")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String originalUrl;

    @Size(max = 25, message = "Short code cannot exceed 25 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Short code can only contain alphanumeric characters, hyphens, and underscores")
    private String shortCode; // Optional - will be auto-generated if not provided

    @Size(max = 50, message = "Custom alias cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Custom alias can only contain alphanumeric characters, hyphens, and underscores")
    private String customAlias;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    private Long clickCount;

    private Boolean isActive;

    @Size(max = 255, message = "Password cannot exceed 255 characters")
    private String password;

    private LocalDateTime lastAccessedAt;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    private String ipAddress;

    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    private String userAgent;

    // Computed fields for response
    private String shortUrl;
    private Boolean isExpired;
    private Long daysUntilExpiration;
}