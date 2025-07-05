package com.notificationservice.service;

import com.notificationservice.dto.FileStorageDto;
import com.notificationservice.dto.FileUploadRequestDto;
import com.notificationservice.entity.FileStorage;
import com.notificationservice.entity.StorageProvider;
import com.notificationservice.mapper.FileStorageMapper;
import com.notificationservice.repository.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileStorageService {

    private final FileStorageRepository fileStorageRepository;
    private final FileStorageMapper fileStorageMapper;
    private final FileShareService fileShareService;
    private final FileVersionService fileVersionService;

    // Base storage path
    private static final String BASE_STORAGE_PATH = "storage/files/";

    /**
     * Upload a file
     */
    public FileStorageDto uploadFile(MultipartFile file, FileUploadRequestDto request, String userId) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique file name
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = generateUniqueFileName(originalFileName);

            // Create file path
            String filePath = buildFilePath(userId, uniqueFileName);
            Path storagePath = Paths.get(BASE_STORAGE_PATH + filePath);

            // Ensure directory exists
            Files.createDirectories(storagePath.getParent());

            // Save file to storage
            Files.copy(file.getInputStream(), storagePath);

            // Calculate checksum
            String checksum = calculateChecksum(file.getBytes());

            // Check for duplicate files
            Optional<FileStorage> existingFile = fileStorageRepository
                    .findByChecksumAndOwnerIdAndIsDeletedFalse(checksum, userId)
                    .stream().findFirst();
            if (existingFile.isPresent()) {
                log.info("Duplicate file detected for user: {}, checksum: {}", userId, checksum);
                // You might want to create a reference instead of uploading again
            }

            // Create file storage entity
            FileStorage fileStorage = new FileStorage();
            fileStorage.setName(uniqueFileName);
            fileStorage.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : originalFileName);
            fileStorage.setFilePath(filePath);
            fileStorage.setFileSize(file.getSize());
            fileStorage.setMimeType(file.getContentType());
            fileStorage.setFileExtension(fileExtension);
            fileStorage.setIsFolder(false);
            fileStorage.setOwnerId(userId);
            fileStorage.setParentId(request.getParentId());
            fileStorage.setStorageProvider(StorageProvider.LOCAL);
            fileStorage.setStoragePath(storagePath.toString());
            fileStorage.setChecksum(checksum);
            fileStorage.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
            fileStorage.setMetadata(request.getMetadata());

            // Save to database
            FileStorage savedFile = fileStorageRepository.save(fileStorage);

            // Create initial version
            fileVersionService.createVersion(savedFile, "Initial upload");

            log.info("File uploaded successfully: {} by user: {}", uniqueFileName, userId);

            return fileStorageMapper.toDto(savedFile);

        } catch (IOException e) {
            log.error("Error uploading file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Create a new folder
     */
    public FileStorageDto createFolder(String folderName, Long parentId, String userId) {
        // Check if folder with same name exists in parent
        Optional<FileStorage> existingFolder = fileStorageRepository
                .findByNameAndParentIdAndOwnerIdAndIsDeletedFalse(folderName, parentId, userId);
        if (existingFolder.isPresent()) {
            throw new RuntimeException("Folder with this name already exists");
        }

        // Create folder entity
        FileStorage folder = new FileStorage();
        folder.setName(folderName);
        folder.setDisplayName(folderName);
        folder.setFilePath(buildFolderPath(userId, folderName, parentId));
        folder.setIsFolder(true);
        folder.setOwnerId(userId);
        folder.setParentId(parentId);
        folder.setStorageProvider(StorageProvider.LOCAL);

        FileStorage savedFolder = fileStorageRepository.save(folder);
        log.info("Folder created: {} by user: {}", folderName, userId);

        return fileStorageMapper.toDto(savedFolder);
    }

    /**
     * Get file by ID
     */
    @Transactional(readOnly = true)
    public Optional<FileStorageDto> getFileById(Long fileId, String userId) {
        Optional<FileStorage> file = fileStorageRepository.findById(fileId);

        if (file.isPresent()) {
            FileStorage fileStorage = file.get();

            // Check permissions
            if (!hasAccess(fileStorage, userId)) {
                return Optional.empty();
            }

            // Update last accessed
            fileStorage.setLastAccessed(LocalDateTime.now());
            fileStorageRepository.save(fileStorage);

            return Optional.of(fileStorageMapper.toDto(fileStorage));
        }

        return Optional.empty();
    }

    /**
     * Get files by parent folder
     */
    @Transactional(readOnly = true)
    public Page<FileStorageDto> getFilesByParent(Long parentId, String userId, Pageable pageable) {
        Page<FileStorage> files = fileStorageRepository.findByParentIdAndIsDeletedFalse(parentId, pageable);
        return files.map(fileStorageMapper::toDto);
    }

    /**
     * Get user's files
     */
    @Transactional(readOnly = true)
    public Page<FileStorageDto> getUserFiles(String userId, Pageable pageable) {
        Page<FileStorage> files = fileStorageRepository.findByOwnerIdAndIsDeletedFalse(userId, pageable);
        return files.map(fileStorageMapper::toDto);
    }

    /**
     * Search files
     */
    @Transactional(readOnly = true)
    public Page<FileStorageDto> searchFiles(String searchTerm, String userId, Pageable pageable) {
        Page<FileStorage> files = fileStorageRepository.searchByName(searchTerm, userId, pageable);
        return files.map(fileStorageMapper::toDto);
    }

    /**
     * Delete file (soft delete)
     */
    public void deleteFile(Long fileId, String userId) {
        Optional<FileStorage> file = fileStorageRepository.findById(fileId);

        if (file.isPresent() && file.get().getOwnerId().equals(userId)) {
            FileStorage fileStorage = file.get();
            fileStorage.setIsDeleted(true);
            fileStorageRepository.save(fileStorage);
            log.info("File deleted: {} by user: {}", fileStorage.getName(), userId);
        } else {
            throw new RuntimeException("File not found or access denied");
        }
    }

    /**
     * Rename file
     */
    public FileStorageDto renameFile(Long fileId, String newName, String userId) {
        Optional<FileStorage> file = fileStorageRepository.findById(fileId);

        if (file.isPresent() && file.get().getOwnerId().equals(userId)) {
            FileStorage fileStorage = file.get();
            fileStorage.setDisplayName(newName);
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            log.info("File renamed: {} to {} by user: {}", fileStorage.getName(), newName, userId);
            return fileStorageMapper.toDto(savedFile);
        } else {
            throw new RuntimeException("File not found or access denied");
        }
    }

    /**
     * Move file to different folder
     */
    public FileStorageDto moveFile(Long fileId, Long newParentId, String userId) {
        Optional<FileStorage> file = fileStorageRepository.findById(fileId);

        if (file.isPresent() && file.get().getOwnerId().equals(userId)) {
            FileStorage fileStorage = file.get();
            fileStorage.setParentId(newParentId);
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            log.info("File moved: {} to parent: {} by user: {}", fileStorage.getName(), newParentId, userId);
            return fileStorageMapper.toDto(savedFile);
        } else {
            throw new RuntimeException("File not found or access denied");
        }
    }

    /**
     * Get file statistics
     */
    @Transactional(readOnly = true)
    public FileStatistics getFileStatistics(String userId) {
        long totalFiles = fileStorageRepository.countByOwnerIdAndIsDeletedFalse(userId);
        long totalFolders = fileStorageRepository.countByOwnerIdAndIsFolderTrueAndIsDeletedFalse(userId);
        Long totalSize = fileStorageRepository.getTotalStorageUsed(userId);

        return new FileStatistics(totalFiles, totalFolders, totalSize != null ? totalSize : 0L);
    }

    // Helper methods
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Add more validation as needed (file size, type, etc.)
        if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
            throw new RuntimeException("File size exceeds limit");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.length() - extension.length());
        return baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String buildFilePath(String userId, String fileName) {
        return userId + "/" + LocalDateTime.now().getYear() + "/" +
                LocalDateTime.now().getMonthValue() + "/" + fileName;
    }

    private String buildFolderPath(String userId, String folderName, Long parentId) {
        // Implement folder path building logic
        return userId + "/folders/" + folderName;
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    private boolean hasAccess(FileStorage file, String userId) {
        // Check if user is owner
        if (file.getOwnerId().equals(userId)) {
            return true;
        }

        // Check if file is public
        if (file.getIsPublic()) {
            return true;
        }

        // Check if file is shared with user
        return fileShareService.hasAccess(file.getId(), userId);
    }

    // Statistics class
    public static class FileStatistics {
        private final long totalFiles;
        private final long totalFolders;
        private final long totalSize;

        public FileStatistics(long totalFiles, long totalFolders, long totalSize) {
            this.totalFiles = totalFiles;
            this.totalFolders = totalFolders;
            this.totalSize = totalSize;
        }

        // Getters
        public long getTotalFiles() {
            return totalFiles;
        }

        public long getTotalFolders() {
            return totalFolders;
        }

        public long getTotalSize() {
            return totalSize;
        }
    }
}