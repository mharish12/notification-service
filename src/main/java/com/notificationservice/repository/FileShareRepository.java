package com.notificationservice.repository;

import com.notificationservice.entity.FileShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    // Find shares by file ID
    List<FileShare> findByFileIdAndIsActiveTrue(Long fileId);

    // Find shares by shared with user
    Page<FileShare> findBySharedWithAndIsActiveTrue(String sharedWith, Pageable pageable);

    // Find shares by shared by user
    Page<FileShare> findBySharedByAndIsActiveTrue(String sharedBy, Pageable pageable);

    // Find share by file and user
    Optional<FileShare> findByFileIdAndSharedWithAndIsActiveTrue(Long fileId, String sharedWith);

    // Find share by access token
    Optional<FileShare> findByAccessTokenAndIsActiveTrue(String accessToken);

    // Find share by share link
    Optional<FileShare> findByShareLinkAndIsActiveTrue(String shareLink);

    // Find expired shares
    @Query("SELECT fs FROM FileShare fs WHERE fs.expiresAt < :now AND fs.isActive = true")
    List<FileShare> findExpiredShares(@Param("now") LocalDateTime now);

    // Find shares by type
    List<FileShare> findByShareTypeAndIsActiveTrue(FileShare.ShareType shareType);

    // Find shares by permission level
    List<FileShare> findByPermissionLevelAndIsActiveTrue(FileShare.PermissionLevel permissionLevel);

    // Count active shares by file
    long countByFileIdAndIsActiveTrue(Long fileId);

    // Count active shares by user
    long countBySharedWithAndIsActiveTrue(String sharedWith);

    // Find shares that expire soon
    @Query("SELECT fs FROM FileShare fs WHERE fs.expiresAt BETWEEN :now AND :future AND fs.isActive = true")
    List<FileShare> findSharesExpiringSoon(@Param("now") LocalDateTime now, @Param("future") LocalDateTime future);

    // Find public shares
    @Query("SELECT fs FROM FileShare fs WHERE fs.shareType = 'PUBLIC' AND fs.isActive = true")
    Page<FileShare> findPublicShares(Pageable pageable);

    // Find team shares
    @Query("SELECT fs FROM FileShare fs WHERE fs.shareType = 'TEAM' AND fs.isActive = true")
    Page<FileShare> findTeamShares(Pageable pageable);
}