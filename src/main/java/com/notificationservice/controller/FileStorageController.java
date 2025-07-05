package com.notificationservice.controller;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.aspect.RateLimitType;
import com.notificationservice.dto.FileShareRequestDto;
import com.notificationservice.dto.FileStorageDto;
import com.notificationservice.dto.FileUploadRequestDto;
import com.notificationservice.entity.FileShare;
import com.notificationservice.entity.FileVersion;
import com.notificationservice.service.FileShareService;
import com.notificationservice.service.FileStorageService;
import com.notificationservice.service.FileVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Storage", description = "File storage operations like upload, download, and management")
public class FileStorageController {

    private final FileStorageService fileStorageService;
    private final FileShareService fileShareService;
    private final FileVersionService fileVersionService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a new file to the storage system")
    @RateLimited(type = RateLimitType.CREATE)
    public ResponseEntity<FileStorageDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "metadata", required = false) String metadata,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileUploadRequestDto request = new FileUploadRequestDto();
            request.setDisplayName(displayName);
            request.setParentId(parentId);
            request.setIsPublic(isPublic);
            request.setMetadata(metadata);

            FileStorageDto uploadedFile = fileStorageService.uploadFile(file, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFile);

        } catch (Exception e) {
            log.error("Error uploading file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/folders")
    @Operation(summary = "Create a folder", description = "Create a new folder in the storage system")
    @RateLimited(type = RateLimitType.CREATE)
    public ResponseEntity<FileStorageDto> createFolder(
            @RequestParam("name") String folderName,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileStorageDto folder = fileStorageService.createFolder(folderName, parentId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(folder);

        } catch (Exception e) {
            log.error("Error creating folder: {}", folderName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Get file details", description = "Get detailed information about a specific file")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<FileStorageDto> getFile(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") String userId) {

        Optional<FileStorageDto> file = fileStorageService.getFileById(fileId, userId);
        return file.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/folder/{parentId}")
    @Operation(summary = "Get folder contents", description = "Get all files and folders within a specific folder")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<Page<FileStorageDto>> getFolderContents(
            @PathVariable Long parentId,
            @RequestHeader("X-User-ID") String userId,
            Pageable pageable) {

        Page<FileStorageDto> files = fileStorageService.getFilesByParent(parentId, userId, pageable);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/my-files")
    @Operation(summary = "Get user files", description = "Get all files owned by the current user")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<Page<FileStorageDto>> getMyFiles(
            @RequestHeader("X-User-ID") String userId,
            Pageable pageable) {

        Page<FileStorageDto> files = fileStorageService.getUserFiles(userId, pageable);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/search")
    @Operation(summary = "Search files", description = "Search for files by name")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<Page<FileStorageDto>> searchFiles(
            @RequestParam("q") String searchTerm,
            @RequestHeader("X-User-ID") String userId,
            Pageable pageable) {

        Page<FileStorageDto> files = fileStorageService.searchFiles(searchTerm, userId, pageable);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete file", description = "Delete a file (soft delete)")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") String userId) {

        try {
            fileStorageService.deleteFile(fileId, userId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{fileId}/rename")
    @Operation(summary = "Rename file", description = "Rename a file or folder")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<FileStorageDto> renameFile(
            @PathVariable Long fileId,
            @RequestParam("name") String newName,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileStorageDto renamedFile = fileStorageService.renameFile(fileId, newName, userId);
            return ResponseEntity.ok(renamedFile);

        } catch (Exception e) {
            log.error("Error renaming file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{fileId}/move")
    @Operation(summary = "Move file", description = "Move a file to a different folder")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<FileStorageDto> moveFile(
            @PathVariable Long fileId,
            @RequestParam("parentId") Long newParentId,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileStorageDto movedFile = fileStorageService.moveFile(fileId, newParentId, userId);
            return ResponseEntity.ok(movedFile);

        } catch (Exception e) {
            log.error("Error moving file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get file statistics", description = "Get storage statistics for the current user")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<FileStorageService.FileStatistics> getStatistics(
            @RequestHeader("X-User-ID") String userId) {

        FileStorageService.FileStatistics stats = fileStorageService.getFileStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    // File sharing endpoints
    @PostMapping("/{fileId}/share")
    @Operation(summary = "Share file", description = "Share a file with another user or create a public link")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<FileShare> shareFile(
            @PathVariable Long fileId,
            @RequestBody FileShareRequestDto request,
            @RequestHeader("X-User-ID") String userId) {

        try {
            request.setFileId(fileId);
            FileShare share = fileShareService.shareFile(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(share);

        } catch (Exception e) {
            log.error("Error sharing file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/shared-with-me")
    @Operation(summary = "Get shared files", description = "Get files shared with the current user")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<List<FileShare>> getSharedWithMe(
            @RequestHeader("X-User-ID") String userId) {

        List<FileShare> shares = fileShareService.getSharedWithUser(userId);
        return ResponseEntity.ok(shares);
    }

    @GetMapping("/shared-by-me")
    @Operation(summary = "Get my shared files", description = "Get files shared by the current user")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<List<FileShare>> getSharedByMe(
            @RequestHeader("X-User-ID") String userId) {

        List<FileShare> shares = fileShareService.getSharedByUser(userId);
        return ResponseEntity.ok(shares);
    }

    @DeleteMapping("/shares/{shareId}")
    @Operation(summary = "Revoke share", description = "Revoke a file share")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long shareId,
            @RequestHeader("X-User-ID") String userId) {

        try {
            fileShareService.revokeShare(shareId, userId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error revoking share: {}", shareId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // File versioning endpoints
    @GetMapping("/{fileId}/versions")
    @Operation(summary = "Get file versions", description = "Get all versions of a file")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<List<FileVersion>> getFileVersions(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") String userId) {

        // Check if user has access to the file
        Optional<FileStorageDto> file = fileStorageService.getFileById(fileId, userId);
        if (file.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<FileVersion> versions = fileVersionService.getFileVersions(fileId);
        return ResponseEntity.ok(versions);
    }

    @PostMapping("/{fileId}/versions/{versionNumber}/restore")
    @Operation(summary = "Restore version", description = "Restore a file to a specific version")
    @RateLimited(type = RateLimitType.ADMIN)
    public ResponseEntity<FileVersion> restoreVersion(
            @PathVariable Long fileId,
            @PathVariable Integer versionNumber,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileVersion restoredVersion = fileVersionService.restoreVersion(fileId, versionNumber, userId);
            return ResponseEntity.ok(restoredVersion);

        } catch (Exception e) {
            log.error("Error restoring version: {} for file: {}", versionNumber, fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}/versions/compare")
    @Operation(summary = "Compare versions", description = "Compare two versions of a file")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<FileVersionService.VersionComparison> compareVersions(
            @PathVariable Long fileId,
            @RequestParam("v1") Integer version1,
            @RequestParam("v2") Integer version2,
            @RequestHeader("X-User-ID") String userId) {

        try {
            FileVersionService.VersionComparison comparison = fileVersionService.compareVersions(fileId, version1,
                    version2);
            return ResponseEntity.ok(comparison);

        } catch (Exception e) {
            log.error("Error comparing versions: {} and {} for file: {}", version1, version2, fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}/versions/statistics")
    @Operation(summary = "Get version statistics", description = "Get statistics about file versions")
    @RateLimited(type = RateLimitType.READ)
    public ResponseEntity<FileVersionService.VersionStatistics> getVersionStatistics(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") String userId) {

        // Check if user has access to the file
        Optional<FileStorageDto> file = fileStorageService.getFileById(fileId, userId);
        if (file.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileVersionService.VersionStatistics stats = fileVersionService.getVersionStatistics(fileId);
        return ResponseEntity.ok(stats);
    }
}