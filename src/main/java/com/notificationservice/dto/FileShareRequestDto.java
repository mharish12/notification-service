package com.notificationservice.dto;

import com.notificationservice.entity.FileShare;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileShareRequestDto {
    private Long fileId;
    private String sharedWith; // User ID or email
    private FileShare.ShareType shareType;
    private FileShare.PermissionLevel permissionLevel;
    private LocalDateTime expiresAt;
    private String message;
}