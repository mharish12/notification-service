package com.notificationservice.mapper;

import com.notificationservice.config.UrlShortenerConfig;
import com.notificationservice.dto.UrlShortenerDto;
import com.notificationservice.entity.UrlShortener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UrlShortenerMapper {

    private final UrlShortenerConfig urlShortenerConfig;

    public UrlShortenerDto toDto(UrlShortener entity) {
        if (entity == null) {
            return null;
        }

        UrlShortenerDto dto = UrlShortenerDto.builder()
                .id(entity.getId())
                .originalUrl(entity.getOriginalUrl())
                .shortCode(entity.getShortCode())
                .customAlias(entity.getCustomAlias())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .expiresAt(entity.getExpiresAt())
                .clickCount(entity.getClickCount())
                .isActive(entity.getIsActive())
                .password(entity.getPassword())
                .lastAccessedAt(entity.getLastAccessedAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .shortUrl(buildShortUrl(entity.getShortCode()))
                .isExpired(entity.isExpired())
                .daysUntilExpiration(calculateDaysUntilExpiration(entity.getExpiresAt()))
                .build();

        // Set audit fields directly since BaseAuditableDto doesn't have @Builder
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedAt(entity.getModifiedAt());
        dto.setModifiedBy(entity.getModifiedBy());

        return dto;
    }

    public UrlShortener toEntity(UrlShortenerDto dto) {
        if (dto == null) {
            return null;
        }

        return UrlShortener.builder()
                .id(dto.getId())
                .originalUrl(dto.getOriginalUrl())
                .shortCode(dto.getShortCode())
                .customAlias(dto.getCustomAlias())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .expiresAt(dto.getExpiresAt())
                .clickCount(dto.getClickCount() != null ? dto.getClickCount() : 0L)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .password(dto.getPassword())
                .lastAccessedAt(dto.getLastAccessedAt())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .build();
    }

    public void updateEntityFromDto(UrlShortener entity, UrlShortenerDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        if (dto.getOriginalUrl() != null) {
            entity.setOriginalUrl(dto.getOriginalUrl());
        }
        if (dto.getShortCode() != null) {
            entity.setShortCode(dto.getShortCode());
        }
        if (dto.getCustomAlias() != null) {
            entity.setCustomAlias(dto.getCustomAlias());
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getExpiresAt() != null) {
            entity.setExpiresAt(dto.getExpiresAt());
        }
        if (dto.getClickCount() != null) {
            entity.setClickCount(dto.getClickCount());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }
        if (dto.getLastAccessedAt() != null) {
            entity.setLastAccessedAt(dto.getLastAccessedAt());
        }
        if (dto.getIpAddress() != null) {
            entity.setIpAddress(dto.getIpAddress());
        }
        if (dto.getUserAgent() != null) {
            entity.setUserAgent(dto.getUserAgent());
        }
    }

    private String buildShortUrl(String shortCode) {
        return urlShortenerConfig.getShortUrl(shortCode);
    }

    private Long calculateDaysUntilExpiration(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
    }
}