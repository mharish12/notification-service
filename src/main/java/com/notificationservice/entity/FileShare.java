package com.notificationservice.entity;

import com.notificationservice.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_shares")
@EqualsAndHashCode(callSuper = true)
public class FileShare extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "shared_by", nullable = false)
    private String sharedBy;

    @Column(name = "shared_with")
    private String sharedWith; // User ID or email

    @Column(name = "share_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShareType shareType;

    @Column(name = "permission_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel;

    @Column(name = "share_link")
    private String shareLink;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    public enum ShareType {
        USER, // Shared with specific user
        LINK, // Shared via public link
        TEAM, // Shared with team
        PUBLIC // Public access
    }

    public enum PermissionLevel {
        READ, // Read only
        WRITE, // Read and write
        ADMIN // Full access including sharing
    }
}