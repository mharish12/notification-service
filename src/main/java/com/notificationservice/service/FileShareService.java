package com.notificationservice.service;

import com.notificationservice.dto.FileShareRequestDto;
import com.notificationservice.entity.FileShare;
import com.notificationservice.entity.FileStorage;
import com.notificationservice.repository.FileShareRepository;
import com.notificationservice.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileShareService {

    private final FileShareRepository fileShareRepository;
    private final FileStorageRepository fileStorageRepository;

    /**
     * Share a file with another user
     */
    public FileShare shareFile(FileShareRequestDto request, String sharedBy) {
        // Verify file exists and user has permission
        Optional<FileStorage> file = fileStorageRepository.findById(request.getFileId());
        if (file.isEmpty() || !file.get().getOwnerId().equals(sharedBy)) {
            throw new RuntimeException("File not found or access denied");
        }

        // Check if already shared
        Optional<FileShare> existingShare = fileShareRepository.findByFileIdAndSharedWithAndIsActiveTrue(
                request.getFileId(), request.getSharedWith());

        if (existingShare.isPresent()) {
            throw new RuntimeException("File is already shared with this user");
        }

        // Create share
        FileShare fileShare = new FileShare();
        fileShare.setFileId(request.getFileId());
        fileShare.setSharedBy(sharedBy);
        fileShare.setSharedWith(request.getSharedWith());
        fileShare.setShareType(request.getShareType());
        fileShare.setPermissionLevel(request.getPermissionLevel());
        fileShare.setExpiresAt(request.getExpiresAt());
        fileShare.setIsActive(true);

        // Generate share link if needed
        if (request.getShareType() == FileShare.ShareType.LINK ||
                request.getShareType() == FileShare.ShareType.PUBLIC) {
            fileShare.setShareLink(generateShareLink());
            fileShare.setAccessToken(generateAccessToken());
        }

        FileShare savedShare = fileShareRepository.save(fileShare);

        // Update file to mark as shared
        FileStorage fileStorage = file.get();
        fileStorage.setIsShared(true);
        fileStorageRepository.save(fileStorage);

        log.info("File shared: {} with {} by {}", request.getFileId(), request.getSharedWith(), sharedBy);
        return savedShare;
    }

    /**
     * Check if user has access to file
     */
    @Transactional(readOnly = true)
    public boolean hasAccess(Long fileId, String userId) {
        List<FileShare> shares = fileShareRepository.findByFileIdAndIsActiveTrue(fileId);

        for (FileShare share : shares) {
            if (share.getSharedWith().equals(userId) && share.getIsActive()) {
                // Check if share has expired
                if (share.getExpiresAt() == null || share.getExpiresAt().isAfter(LocalDateTime.now())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get user's shared files
     */
    @Transactional(readOnly = true)
    public List<FileShare> getSharedWithUser(String userId) {
        return fileShareRepository.findBySharedWithAndIsActiveTrue(userId, null).getContent();
    }

    /**
     * Get files shared by user
     */
    @Transactional(readOnly = true)
    public List<FileShare> getSharedByUser(String userId) {
        return fileShareRepository.findBySharedByAndIsActiveTrue(userId, null).getContent();
    }

    /**
     * Revoke file share
     */
    public void revokeShare(Long shareId, String userId) {
        Optional<FileShare> share = fileShareRepository.findById(shareId);

        if (share.isPresent() && share.get().getSharedBy().equals(userId)) {
            FileShare fileShare = share.get();
            fileShare.setIsActive(false);
            fileShareRepository.save(fileShare);

            log.info("Share revoked: {} by {}", shareId, userId);
        } else {
            throw new RuntimeException("Share not found or access denied");
        }
    }

    /**
     * Get share by access token
     */
    @Transactional(readOnly = true)
    public Optional<FileShare> getShareByToken(String accessToken) {
        return fileShareRepository.findByAccessTokenAndIsActiveTrue(accessToken);
    }

    /**
     * Get share by share link
     */
    @Transactional(readOnly = true)
    public Optional<FileShare> getShareByLink(String shareLink) {
        return fileShareRepository.findByShareLinkAndIsActiveTrue(shareLink);
    }

    /**
     * Update share permissions
     */
    public FileShare updateSharePermissions(Long shareId, FileShare.PermissionLevel permissionLevel, String userId) {
        Optional<FileShare> share = fileShareRepository.findById(shareId);

        if (share.isPresent() && share.get().getSharedBy().equals(userId)) {
            FileShare fileShare = share.get();
            fileShare.setPermissionLevel(permissionLevel);
            FileShare savedShare = fileShareRepository.save(fileShare);

            log.info("Share permissions updated: {} to {} by {}", shareId, permissionLevel, userId);
            return savedShare;
        } else {
            throw new RuntimeException("Share not found or access denied");
        }
    }

    /**
     * Clean up expired shares
     */
    public void cleanupExpiredShares() {
        List<FileShare> expiredShares = fileShareRepository.findExpiredShares(LocalDateTime.now());

        for (FileShare share : expiredShares) {
            share.setIsActive(false);
            fileShareRepository.save(share);
        }

        log.info("Cleaned up {} expired shares", expiredShares.size());
    }

    // Helper methods
    private String generateShareLink() {
        return "https://files.example.com/share/" + UUID.randomUUID().toString();
    }

    private String generateAccessToken() {
        return UUID.randomUUID().toString();
    }
}