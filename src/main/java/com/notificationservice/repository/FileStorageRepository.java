package com.notificationservice.repository;

import com.notificationservice.entity.FileStorage;
import com.notificationservice.entity.StorageProvider;
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
public interface FileStorageRepository extends JpaRepository<FileStorage, Long> {

    // Find files by owner
    Page<FileStorage> findByOwnerIdAndIsDeletedFalse(String ownerId, Pageable pageable);

    // Find files by parent folder
    List<FileStorage> findByParentIdAndIsDeletedFalse(Long parentId);

    // Find files by parent folder with pagination
    Page<FileStorage> findByParentIdAndIsDeletedFalse(Long parentId, Pageable pageable);

    // Find folders by owner
    List<FileStorage> findByOwnerIdAndIsFolderTrueAndIsDeletedFalse(String ownerId);

    // Find by path
    Optional<FileStorage> findByFilePathAndOwnerIdAndIsDeletedFalse(String filePath, String ownerId);

    // Find by name in same folder
    Optional<FileStorage> findByNameAndParentIdAndOwnerIdAndIsDeletedFalse(String name, Long parentId, String ownerId);

    // Find shared files
    @Query("SELECT f FROM FileStorage f WHERE f.isShared = true AND f.isDeleted = false")
    Page<FileStorage> findSharedFiles(Pageable pageable);

    // Find public files
    @Query("SELECT f FROM FileStorage f WHERE f.isPublic = true AND f.isDeleted = false")
    Page<FileStorage> findPublicFiles(Pageable pageable);

    // Find files by storage provider
    List<FileStorage> findByStorageProvider(StorageProvider storageProvider);

    // Find files by mime type
    Page<FileStorage> findByMimeTypeContainingAndOwnerIdAndIsDeletedFalse(String mimeType, String ownerId,
            Pageable pageable);

    // Find files by name (search)
    @Query("SELECT f FROM FileStorage f WHERE f.name LIKE %:searchTerm% AND f.ownerId = :ownerId AND f.isDeleted = false")
    Page<FileStorage> searchByName(@Param("searchTerm") String searchTerm, @Param("ownerId") String ownerId,
            Pageable pageable);

    // Find files by display name (search)
    @Query("SELECT f FROM FileStorage f WHERE f.displayName LIKE %:searchTerm% AND f.ownerId = :ownerId AND f.isDeleted = false")
    Page<FileStorage> searchByDisplayName(@Param("searchTerm") String searchTerm, @Param("ownerId") String ownerId,
            Pageable pageable);

    // Find recently accessed files
    @Query("SELECT f FROM FileStorage f WHERE f.ownerId = :ownerId AND f.isDeleted = false AND f.lastAccessed IS NOT NULL ORDER BY f.lastAccessed DESC")
    Page<FileStorage> findRecentlyAccessed(@Param("ownerId") String ownerId, Pageable pageable);

    // Find files created in date range
    @Query("SELECT f FROM FileStorage f WHERE f.ownerId = :ownerId AND f.isDeleted = false AND f.createdAt BETWEEN :startDate AND :endDate")
    Page<FileStorage> findByDateRange(@Param("ownerId") String ownerId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Count files by owner
    long countByOwnerIdAndIsDeletedFalse(String ownerId);

    // Count folders by owner
    long countByOwnerIdAndIsFolderTrueAndIsDeletedFalse(String ownerId);

    // Get total storage used by owner
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileStorage f WHERE f.ownerId = :ownerId AND f.isDeleted = false AND f.isFolder = false")
    Long getTotalStorageUsed(@Param("ownerId") String ownerId);

    // Find files by checksum (for deduplication)
    List<FileStorage> findByChecksumAndOwnerIdAndIsDeletedFalse(String checksum, String ownerId);
}