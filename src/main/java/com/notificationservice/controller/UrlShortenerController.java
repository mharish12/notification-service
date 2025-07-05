package com.notificationservice.controller;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.aspect.RateLimitType;
import com.notificationservice.dto.UrlShortenerDto;
import com.notificationservice.service.HelperService;
import com.notificationservice.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/url-shortener")
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final HelperService helperService;

    @PostMapping
    @RateLimited(type = RateLimitType.CREATE)
    public ResponseEntity<UrlShortenerDto> createUrlShortener(@Valid @RequestBody UrlShortenerDto dto) {
        log.info("Creating URL shortener for: {}", dto.getOriginalUrl());
        UrlShortenerDto created = urlShortenerService.createUrlShortener(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<UrlShortenerDto> getUrlShortenerById(@PathVariable Long id) {
        log.info("Getting URL shortener by ID: {}", id);
        Optional<UrlShortenerDto> dto = urlShortenerService.getUrlShortenerById(id);
        return dto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{shortCode}")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<UrlShortenerDto> getUrlShortenerByShortCode(@PathVariable String shortCode) {
        log.info("Getting URL shortener by short code: {}", shortCode);
        Optional<UrlShortenerDto> dto = urlShortenerService.getUrlShortenerByShortCode(shortCode);
        return dto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/alias/{customAlias}")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<UrlShortenerDto> getUrlShortenerByCustomAlias(@PathVariable String customAlias) {
        log.info("Getting URL shortener by custom alias: {}", customAlias);
        Optional<UrlShortenerDto> dto = urlShortenerService.getUrlShortenerByCustomAlias(customAlias);
        return dto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<List<UrlShortenerDto>> getAllUrlShorteners(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        log.info("Getting all URL shorteners, active only: {}", activeOnly);
        List<UrlShortenerDto> dtos = activeOnly ? urlShortenerService.getActiveUrlShorteners()
                : urlShortenerService.getAllUrlShorteners();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<UrlShortenerDto> updateUrlShortener(
            @PathVariable Long id,
            @Valid @RequestBody UrlShortenerDto dto) {
        log.info("Updating URL shortener with ID: {}", id);
        try {
            UrlShortenerDto updated = urlShortenerService.updateUrlShortener(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error updating URL shortener: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<Void> deleteUrlShortener(@PathVariable Long id) {
        log.info("Deleting URL shortener with ID: {}", id);
        try {
            urlShortenerService.deleteUrlShortener(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting URL shortener: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/deactivate")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<Void> deactivateUrlShortener(@PathVariable Long id) {
        log.info("Deactivating URL shortener with ID: {}", id);
        try {
            urlShortenerService.deactivateUrlShortener(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deactivating URL shortener: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/redirect/{shortCode}")
    @RateLimited(type = RateLimitType.REDIRECT)
    public ResponseEntity<Map<String, String>> redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        log.info("Redirecting short code: {}", shortCode);

        try {
            String ipAddress = helperService.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            String originalUrl = urlShortenerService.redirectToOriginalUrl(shortCode, ipAddress, userAgent);

            return ResponseEntity.ok(Map.of("originalUrl", originalUrl));
        } catch (IllegalArgumentException e) {
            log.error("Short code not found: {}", shortCode);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("URL has expired: {}", shortCode);
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
    }

    @GetMapping("/analytics/top")
    @RateLimited(type = RateLimitType.ANALYTICS)
    public ResponseEntity<List<UrlShortenerDto>> getTopUrlsByClickCount(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top URLs by click count, limit: {}", limit);
        List<UrlShortenerDto> dtos = urlShortenerService.getTopUrlsByClickCount(limit);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/analytics/recent")
    @RateLimited(type = RateLimitType.ANALYTICS)
    public ResponseEntity<List<UrlShortenerDto>> getRecentUrls(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting URLs created in the last {} days", days);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<UrlShortenerDto> dtos = urlShortenerService.getUrlsCreatedAfter(startDate);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/analytics/stats")
    @RateLimited(type = RateLimitType.ANALYTICS)
    public ResponseEntity<Map<String, Object>> getAnalyticsStats() {
        log.info("Getting analytics statistics");

        Long activeUrlCount = urlShortenerService.getActiveUrlCount();
        Long totalClicks = urlShortenerService.getTotalClicks();

        Map<String, Object> stats = Map.of(
                "activeUrlCount", activeUrlCount != null ? activeUrlCount : 0L,
                "totalClicks", totalClicks != null ? totalClicks : 0L,
                "timestamp", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/cleanup/expired")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<Map<String, String>> cleanupExpiredUrls() {
        log.info("Cleaning up expired URLs");
        urlShortenerService.cleanupExpiredUrls();
        return ResponseEntity.ok(Map.of("message", "Expired URLs cleanup completed"));
    }
}