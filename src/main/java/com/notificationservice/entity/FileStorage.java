package com.notificationservice.entity;

import com.notificationservice.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_storage")
@EqualsAndHashCode(callSuper = true)
public class FileStorage extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "is_folder", nullable = false)
    private Boolean isFolder = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "storage_provider")
    @Enumerated(EnumType.STRING)
    private StorageProvider storageProvider = StorageProvider.LOCAL;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional metadata
}