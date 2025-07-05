package com.notificationservice.dto;

import com.notificationservice.entity.StorageProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileStorageDto extends BaseAuditableDto {

    private Long id;
    private String name;
    private String displayName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private String fileExtension;
    private Boolean isFolder;
    private Boolean isDeleted;
    private Boolean isShared;
    private Boolean isPublic;
    private Long parentId;
    private String ownerId;
    private StorageProvider storageProvider;
    private String storagePath;
    private String checksum;
    private Integer version;
    private LocalDateTime lastAccessed;
    private String thumbnailPath;
    private String metadata;

    // Additional fields for UI
    private String parentPath;
    private String ownerName;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canShare;
    private Integer childCount; // For folders
    private String shareLink;
}