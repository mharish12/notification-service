package com.notificationservice.repository;

import com.notificationservice.entity.UrlShortener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlShortenerRepository extends JpaRepository<UrlShortener, Long> {

    Optional<UrlShortener> findByShortCodeAndIsActiveTrue(String shortCode);

    Optional<UrlShortener> findByCustomAliasAndIsActiveTrue(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    @Query("SELECT u FROM UrlShortener u WHERE u.expiresAt < :now AND u.isActive = true")
    List<UrlShortener> findExpiredUrls(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM UrlShortener u WHERE u.originalUrl = :originalUrl AND u.isActive = true")
    List<UrlShortener> findByOriginalUrl(@Param("originalUrl") String originalUrl);

    @Query("SELECT u FROM UrlShortener u WHERE u.isActive = true ORDER BY u.clickCount DESC")
    List<UrlShortener> findTopUrlsByClickCount();

    @Query("SELECT u FROM UrlShortener u WHERE u.createdAt >= :startDate AND u.isActive = true")
    List<UrlShortener> findUrlsCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM UrlShortener u WHERE u.isActive = true")
    Long countActiveUrls();

    @Query("SELECT SUM(u.clickCount) FROM UrlShortener u WHERE u.isActive = true")
    Long getTotalClicks();
}