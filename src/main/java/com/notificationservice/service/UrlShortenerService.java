package com.notificationservice.service;

import com.notificationservice.dto.UrlShortenerDto;
import com.notificationservice.entity.UrlShortener;
import com.notificationservice.mapper.UrlShortenerMapper;
import com.notificationservice.repository.UrlShortenerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UrlShortenerService {

    private final UrlShortenerRepository urlShortenerRepository;
    private final UrlShortenerMapper urlShortenerMapper;

    public UrlShortenerDto createUrlShortener(UrlShortenerDto dto) {
        log.info("Creating URL shortener for: {}", dto.getOriginalUrl());

        // Check if custom alias is provided and if it's unique
        if (dto.getCustomAlias() != null && !dto.getCustomAlias().isEmpty()) {
            if (urlShortenerRepository.existsByCustomAlias(dto.getCustomAlias())) {
                throw new IllegalArgumentException("Custom alias already exists: " + dto.getCustomAlias());
            }
        }

        // Check if custom short code is provided and if it's unique
        if (dto.getShortCode() != null && !dto.getShortCode().isEmpty()) {
            if (urlShortenerRepository.existsByShortCode(dto.getShortCode())) {
                throw new IllegalArgumentException("Short code already exists: " + dto.getShortCode());
            }
        }

        UrlShortener entity = urlShortenerMapper.toEntity(dto);

        // If no short code is provided, the entity will auto-generate one in
        // @PrePersist
        // But we need to handle potential conflicts by retrying if the generated code
        // already exists
        UrlShortener savedEntity = saveWithUniqueShortCode(entity);

        log.info("Created URL shortener with ID: {} and short code: {}", savedEntity.getId(),
                savedEntity.getShortCode());
        return urlShortenerMapper.toDto(savedEntity);
    }

    public Optional<UrlShortenerDto> getUrlShortenerById(Long id) {
        return urlShortenerRepository.findById(id)
                .map(urlShortenerMapper::toDto);
    }

    public Optional<UrlShortenerDto> getUrlShortenerByShortCode(String shortCode) {
        return urlShortenerRepository.findByShortCodeAndIsActiveTrue(shortCode)
                .map(urlShortenerMapper::toDto);
    }

    public Optional<UrlShortenerDto> getUrlShortenerByCustomAlias(String customAlias) {
        return urlShortenerRepository.findByCustomAliasAndIsActiveTrue(customAlias)
                .map(urlShortenerMapper::toDto);
    }

    public List<UrlShortenerDto> getAllUrlShorteners() {
        return urlShortenerRepository.findAll().stream()
                .map(urlShortenerMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UrlShortenerDto> getActiveUrlShorteners() {
        return urlShortenerRepository.findAll().stream()
                .filter(UrlShortener::getIsActive)
                .map(urlShortenerMapper::toDto)
                .collect(Collectors.toList());
    }

    public UrlShortenerDto updateUrlShortener(Long id, UrlShortenerDto dto) {
        log.info("Updating URL shortener with ID: {}", id);

        UrlShortener entity = getOrThrowUrlShortenerById(id);

        // Check if trying to update short code or custom alias
        if (dto.getShortCode() != null && !dto.getShortCode().equals(entity.getShortCode())) {
            if (urlShortenerRepository.existsByShortCode(dto.getShortCode())) {
                throw new IllegalArgumentException("Short code already exists: " + dto.getShortCode());
            }
        }

        if (dto.getCustomAlias() != null && !dto.getCustomAlias().equals(entity.getCustomAlias())) {
            if (urlShortenerRepository.existsByCustomAlias(dto.getCustomAlias())) {
                throw new IllegalArgumentException("Custom alias already exists: " + dto.getCustomAlias());
            }
        }

        urlShortenerMapper.updateEntityFromDto(entity, dto);
        UrlShortener updatedEntity = urlShortenerRepository.save(entity);

        log.info("Updated URL shortener with ID: {}", id);
        return urlShortenerMapper.toDto(updatedEntity);
    }

    public void deleteUrlShortener(Long id) {
        log.info("Deleting URL shortener with ID: {}", id);

        UrlShortener entity = getOrThrowUrlShortenerById(id);
        urlShortenerRepository.delete(entity);

        log.info("Deleted URL shortener with ID: {}", id);
    }

    public void deactivateUrlShortener(Long id) {
        log.info("Deactivating URL shortener with ID: {}", id);

        UrlShortener entity = getOrThrowUrlShortenerById(id);
        entity.setIsActive(false);
        urlShortenerRepository.save(entity);

        log.info("Deactivated URL shortener with ID: {}", id);
    }

    public String redirectToOriginalUrl(String shortCode, String ipAddress, String userAgent) {
        log.info("Redirecting short code: {}", shortCode);

        Optional<UrlShortener> urlShortenerOpt = urlShortenerRepository.findByShortCodeAndIsActiveTrue(shortCode);

        if (urlShortenerOpt.isEmpty()) {
            throw new IllegalArgumentException("Short code not found: " + shortCode);
        }

        UrlShortener urlShortener = urlShortenerOpt.get();

        // Check if URL is expired
        if (urlShortener.isExpired()) {
            throw new IllegalStateException("URL has expired: " + shortCode);
        }

        // Check if password protected
        if (urlShortener.getPassword() != null && !urlShortener.getPassword().isEmpty()) {
            // In a real implementation, you would check the password here
            // For now, we'll just log it
            log.warn("Password protected URL accessed: {}", shortCode);
        }

        // Update click count and tracking info
        urlShortener.incrementClickCount();
        urlShortener.setIpAddress(ipAddress);
        urlShortener.setUserAgent(userAgent);
        urlShortenerRepository.save(urlShortener);

        log.info("Redirected short code: {} to: {}", shortCode, urlShortener.getOriginalUrl());
        return urlShortener.getOriginalUrl();
    }

    public List<UrlShortenerDto> getTopUrlsByClickCount(int limit) {
        return urlShortenerRepository.findTopUrlsByClickCount().stream()
                .limit(limit)
                .map(urlShortenerMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UrlShortenerDto> getUrlsCreatedAfter(LocalDateTime startDate) {
        return urlShortenerRepository.findUrlsCreatedAfter(startDate).stream()
                .map(urlShortenerMapper::toDto)
                .collect(Collectors.toList());
    }

    public void cleanupExpiredUrls() {
        log.info("Cleaning up expired URLs");

        List<UrlShortener> expiredUrls = urlShortenerRepository.findExpiredUrls(LocalDateTime.now());

        for (UrlShortener url : expiredUrls) {
            url.setIsActive(false);
            urlShortenerRepository.save(url);
            log.info("Deactivated expired URL: {}", url.getShortCode());
        }

        log.info("Cleaned up {} expired URLs", expiredUrls.size());
    }

    public Long getActiveUrlCount() {
        return urlShortenerRepository.countActiveUrls();
    }

    public Long getTotalClicks() {
        return urlShortenerRepository.getTotalClicks();
    }

    private UrlShortener getOrThrowUrlShortenerById(Long id) {
        return urlShortenerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("URL shortener not found with ID: " + id));
    }

    /**
     * Saves the entity with a unique short code, retrying if there are conflicts
     */
    private UrlShortener saveWithUniqueShortCode(UrlShortener entity) {
        int maxAttempts = 10;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                // If no short code is provided, generate one
                if (entity.getShortCode() == null || entity.getShortCode().isEmpty()) {
                    entity.regenerateShortCode();
                }

                // Check if the short code already exists before trying to save
                if (urlShortenerRepository.existsByShortCode(entity.getShortCode())) {
                    log.warn("Short code '{}' already exists, regenerating... (attempt {})",
                            entity.getShortCode(), attempt + 1);
                    entity.regenerateShortCode();
                    attempt++;
                    continue;
                }

                return urlShortenerRepository.save(entity);
            } catch (Exception e) {
                // Check if it's a duplicate key exception (short code conflict)
                if (isDuplicateKeyException(e)) {
                    log.warn("Short code conflict detected for '{}', regenerating... (attempt {})",
                            entity.getShortCode(), attempt + 1);
                    // Generate a new short code
                    entity.regenerateShortCode();
                    attempt++;
                } else {
                    // If it's not a short code conflict, re-throw the exception
                    throw e;
                }
            }
        }

        throw new RuntimeException("Failed to generate unique short code after " + maxAttempts + " attempts");
    }

    /**
     * Check if the exception is due to a duplicate key constraint violation
     */
    private boolean isDuplicateKeyException(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        // Check for various database-specific duplicate key error messages
        return message.contains("short_code") ||
                message.contains("Duplicate entry") ||
                message.contains("unique constraint") ||
                message.contains("duplicate key value") ||
                message.contains("constraint violation");
    }
}