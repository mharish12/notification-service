package com.notificationservice.repository;

import com.notificationservice.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {

    // Find all versions of a file
    List<FileVersion> findByFileIdOrderByVersionNumberDesc(Long fileId);

    // Find current version of a file
    Optional<FileVersion> findByFileIdAndIsCurrentTrue(Long fileId);

    // Find specific version of a file
    Optional<FileVersion> findByFileIdAndVersionNumber(Long fileId, Integer versionNumber);

    // Find latest version number for a file
    @Query("SELECT MAX(fv.versionNumber) FROM FileVersion fv WHERE fv.fileId = :fileId")
    Optional<Integer> findLatestVersionNumber(@Param("fileId") Long fileId);

    // Count versions for a file
    long countByFileId(Long fileId);

    // Find versions by checksum (for deduplication)
    List<FileVersion> findByChecksum(String checksum);

    // Find old versions (for cleanup)
    @Query("SELECT fv FROM FileVersion fv WHERE fv.fileId = :fileId AND fv.isCurrent = false ORDER BY fv.versionNumber DESC")
    List<FileVersion> findOldVersions(@Param("fileId") Long fileId);
}