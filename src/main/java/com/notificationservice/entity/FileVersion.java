package com.notificationservice.entity;

import com.notificationservice.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "file_versions")
@EqualsAndHashCode(callSuper = true)
public class FileVersion extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum", nullable = false)
    private String checksum;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "storage_provider")
    @Enumerated(EnumType.STRING)
    private StorageProvider storageProvider;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "change_description")
    private String changeDescription;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional metadata
}