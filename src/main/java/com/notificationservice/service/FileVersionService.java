package com.notificationservice.service;

import com.notificationservice.entity.FileStorage;
import com.notificationservice.entity.FileVersion;
import com.notificationservice.entity.StorageProvider;
import com.notificationservice.repository.FileVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileVersionService {

    private final FileVersionRepository fileVersionRepository;

    /**
     * Create a new version of a file
     */
    public FileVersion createVersion(FileStorage file, String changeDescription) {
        // Get the next version number
        Optional<Integer> latestVersion = fileVersionRepository.findLatestVersionNumber(file.getId());
        int newVersionNumber = latestVersion.orElse(0) + 1;

        // Mark previous version as not current
        if (latestVersion.isPresent()) {
            Optional<FileVersion> currentVersion = fileVersionRepository.findByFileIdAndIsCurrentTrue(file.getId());
            if (currentVersion.isPresent()) {
                FileVersion current = currentVersion.get();
                current.setIsCurrent(false);
                fileVersionRepository.save(current);
            }
        }

        // Create new version
        FileVersion fileVersion = new FileVersion();
        fileVersion.setFileId(file.getId());
        fileVersion.setVersionNumber(newVersionNumber);
        fileVersion.setFilePath(file.getFilePath());
        fileVersion.setFileSize(file.getFileSize());
        fileVersion.setChecksum(file.getChecksum());
        fileVersion.setMimeType(file.getMimeType());
        fileVersion.setStorageProvider(file.getStorageProvider());
        fileVersion.setStoragePath(file.getStoragePath());
        fileVersion.setChangeDescription(changeDescription);
        fileVersion.setIsCurrent(true);
        fileVersion.setMetadata(file.getMetadata());

        FileVersion savedVersion = fileVersionRepository.save(fileVersion);

        log.info("Created version {} for file: {}", newVersionNumber, file.getId());
        return savedVersion;
    }

    /**
     * Get all versions of a file
     */
    @Transactional(readOnly = true)
    public List<FileVersion> getFileVersions(Long fileId) {
        return fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId);
    }

    /**
     * Get current version of a file
     */
    @Transactional(readOnly = true)
    public Optional<FileVersion> getCurrentVersion(Long fileId) {
        return fileVersionRepository.findByFileIdAndIsCurrentTrue(fileId);
    }

    /**
     * Get specific version of a file
     */
    @Transactional(readOnly = true)
    public Optional<FileVersion> getVersion(Long fileId, Integer versionNumber) {
        return fileVersionRepository.findByFileIdAndVersionNumber(fileId, versionNumber);
    }

    /**
     * Restore file to a specific version
     */
    public FileVersion restoreVersion(Long fileId, Integer versionNumber, String userId) {
        Optional<FileVersion> targetVersion = fileVersionRepository.findByFileIdAndVersionNumber(fileId, versionNumber);
        if (targetVersion.isEmpty()) {
            throw new RuntimeException("Version not found");
        }

        // Create a new version based on the target version
        FileVersion version = targetVersion.get();
        String changeDescription = "Restored to version " + versionNumber;

        // This would typically involve copying the file content from the target version
        // For now, we'll just create a new version entry
        return createVersionFromVersion(version, changeDescription);
    }

    /**
     * Delete old versions (keep only the last N versions)
     */
    public void cleanupOldVersions(Long fileId, int keepVersions) {
        List<FileVersion> oldVersions = fileVersionRepository.findOldVersions(fileId);

        // Keep only the specified number of versions
        if (oldVersions.size() > keepVersions) {
            List<FileVersion> versionsToDelete = oldVersions.subList(keepVersions, oldVersions.size());

            for (FileVersion version : versionsToDelete) {
                // In a real implementation, you would also delete the actual file
                fileVersionRepository.delete(version);
            }

            log.info("Deleted {} old versions for file: {}", versionsToDelete.size(), fileId);
        }
    }

    /**
     * Compare two versions of a file
     */
    @Transactional(readOnly = true)
    public VersionComparison compareVersions(Long fileId, Integer version1, Integer version2) {
        Optional<FileVersion> v1 = fileVersionRepository.findByFileIdAndVersionNumber(fileId, version1);
        Optional<FileVersion> v2 = fileVersionRepository.findByFileIdAndVersionNumber(fileId, version2);

        if (v1.isEmpty() || v2.isEmpty()) {
            throw new RuntimeException("One or both versions not found");
        }

        FileVersion version1Data = v1.get();
        FileVersion version2Data = v2.get();

        return new VersionComparison(
                version1Data,
                version2Data,
                !version1Data.getChecksum().equals(version2Data.getChecksum()),
                version1Data.getFileSize().equals(version2Data.getFileSize()));
    }

    /**
     * Get version statistics
     */
    @Transactional(readOnly = true)
    public VersionStatistics getVersionStatistics(Long fileId) {
        long totalVersions = fileVersionRepository.countByFileId(fileId);
        Optional<FileVersion> currentVersion = fileVersionRepository.findByFileIdAndIsCurrentTrue(fileId);

        return new VersionStatistics(
                totalVersions,
                currentVersion.map(FileVersion::getVersionNumber).orElse(0));
    }

    // Helper method to create version from existing version
    private FileVersion createVersionFromVersion(FileVersion sourceVersion, String changeDescription) {
        Optional<Integer> latestVersion = fileVersionRepository.findLatestVersionNumber(sourceVersion.getFileId());
        int newVersionNumber = latestVersion.orElse(0) + 1;

        // Mark previous version as not current
        Optional<FileVersion> currentVersion = fileVersionRepository
                .findByFileIdAndIsCurrentTrue(sourceVersion.getFileId());
        if (currentVersion.isPresent()) {
            FileVersion current = currentVersion.get();
            current.setIsCurrent(false);
            fileVersionRepository.save(current);
        }

        // Create new version
        FileVersion fileVersion = new FileVersion();
        fileVersion.setFileId(sourceVersion.getFileId());
        fileVersion.setVersionNumber(newVersionNumber);
        fileVersion.setFilePath(sourceVersion.getFilePath());
        fileVersion.setFileSize(sourceVersion.getFileSize());
        fileVersion.setChecksum(sourceVersion.getChecksum());
        fileVersion.setMimeType(sourceVersion.getMimeType());
        fileVersion.setStorageProvider(sourceVersion.getStorageProvider());
        fileVersion.setStoragePath(sourceVersion.getStoragePath());
        fileVersion.setChangeDescription(changeDescription);
        fileVersion.setIsCurrent(true);
        fileVersion.setMetadata(sourceVersion.getMetadata());

        return fileVersionRepository.save(fileVersion);
    }

    // Data classes
    public static class VersionComparison {
        private final FileVersion version1;
        private final FileVersion version2;
        private final boolean contentChanged;
        private final boolean sizeChanged;

        public VersionComparison(FileVersion version1, FileVersion version2, boolean contentChanged,
                boolean sizeChanged) {
            this.version1 = version1;
            this.version2 = version2;
            this.contentChanged = contentChanged;
            this.sizeChanged = sizeChanged;
        }

        // Getters
        public FileVersion getVersion1() {
            return version1;
        }

        public FileVersion getVersion2() {
            return version2;
        }

        public boolean isContentChanged() {
            return contentChanged;
        }

        public boolean isSizeChanged() {
            return sizeChanged;
        }
    }

    public static class VersionStatistics {
        private final long totalVersions;
        private final int currentVersionNumber;

        public VersionStatistics(long totalVersions, int currentVersionNumber) {
            this.totalVersions = totalVersions;
            this.currentVersionNumber = currentVersionNumber;
        }

        // Getters
        public long getTotalVersions() {
            return totalVersions;
        }

        public int getCurrentVersionNumber() {
            return currentVersionNumber;
        }
    }
}