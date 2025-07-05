package com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_shorteners", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_original_url", columnList = "original_url"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UrlShortener extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "custom_alias", length = 50)
    private String customAlias;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (shortCode == null || shortCode.isEmpty()) {
            this.shortCode = generateShortCode();
        }
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
    }

    private String generateShortCode() {
        // Generate a random 10-character alphanumeric code with more characters
        // Using a larger character set to reduce collision probability
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        // Use SecureRandom for better randomness (fallback to Math.random if not
        // available)
        try {
            java.security.SecureRandom random = new java.security.SecureRandom();
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
        } catch (Exception e) {
            // Fallback to Math.random if SecureRandom is not available
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
        }

        return sb.toString();
    }

    /**
     * Generate a new short code and set it on this entity
     */
    public void regenerateShortCode() {
        this.shortCode = generateShortCode();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementClickCount() {
        this.clickCount++;
        this.lastAccessedAt = LocalDateTime.now();
    }
}