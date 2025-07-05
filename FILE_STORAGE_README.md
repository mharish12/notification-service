# File Storage System

A comprehensive file storage system similar to Dropbox or OneDrive, built as part of the notification service. This system provides file upload, download, sharing, versioning, and organization capabilities.

## Features

### Core File Management

- **File Upload**: Upload files with metadata and organization
- **Folder Creation**: Create and organize files in folders
- **File Operations**: Rename, move, delete files and folders
- **Search**: Search files by name and content
- **Soft Delete**: Files are marked as deleted but not permanently removed

### File Sharing

- **User Sharing**: Share files with specific users
- **Public Links**: Create public shareable links
- **Team Sharing**: Share files with team members
- **Permission Levels**: READ, WRITE, ADMIN permissions
- **Expiration**: Set expiration dates for shared files
- **Access Tracking**: Track downloads and access times

### File Versioning

- **Automatic Versioning**: Every file change creates a new version
- **Version History**: View all versions of a file
- **Version Comparison**: Compare different versions
- **Version Restoration**: Restore files to previous versions
- **Version Statistics**: Track version counts and metadata

### Storage Providers

- **Local Storage**: Files stored on local file system
- **Cloud Storage**: Support for S3, Google Cloud Storage, Azure
- **External Providers**: Integration with Dropbox and OneDrive APIs
- **Deduplication**: Prevent duplicate files using checksums

### Security & Access Control

- **User Authentication**: All operations require user authentication
- **Permission Checking**: Verify user permissions before operations
- **Rate Limiting**: Protect against abuse with rate limiting
- **Audit Trail**: Track all file operations with timestamps

## Architecture

### Entities

#### FileStorage

Main entity representing files and folders:

- File metadata (name, size, type, path)
- Ownership and permissions
- Storage provider information
- Version and access tracking

#### FileShare

Manages file sharing and permissions:

- Share type (user, link, team, public)
- Permission levels (read, write, admin)
- Access tokens and share links
- Expiration and access tracking

#### FileVersion

Tracks file version history:

- Version numbers and metadata
- Change descriptions
- Storage paths for each version
- Current version tracking

### Services

#### FileStorageService

Core file management operations:

- File upload and download
- Folder creation and management
- File operations (rename, move, delete)
- Search and statistics

#### FileShareService

File sharing functionality:

- Create and manage shares
- Permission checking
- Share link generation
- Access token management

#### FileVersionService

File versioning operations:

- Version creation and tracking
- Version comparison
- Version restoration
- Version cleanup

## API Endpoints

### File Management

```
POST   /api/v1/files/upload          # Upload a file
POST   /api/v1/files/folders         # Create a folder
GET    /api/v1/files/{fileId}        # Get file details
GET    /api/v1/files/folder/{parentId} # Get folder contents
GET    /api/v1/files/my-files        # Get user's files
GET    /api/v1/files/search          # Search files
DELETE /api/v1/files/{fileId}        # Delete file
PUT    /api/v1/files/{fileId}/rename # Rename file
PUT    /api/v1/files/{fileId}/move   # Move file
GET    /api/v1/files/statistics      # Get storage statistics
```

### File Sharing

```
POST   /api/v1/files/{fileId}/share  # Share a file
GET    /api/v1/files/shared-with-me  # Get files shared with user
GET    /api/v1/files/shared-by-me    # Get files shared by user
DELETE /api/v1/files/shares/{shareId} # Revoke share
```

### File Versioning

```
GET    /api/v1/files/{fileId}/versions           # Get file versions
POST   /api/v1/files/{fileId}/versions/{version}/restore # Restore version
GET    /api/v1/files/{fileId}/versions/compare   # Compare versions
GET    /api/v1/files/{fileId}/versions/statistics # Get version statistics
```

## Configuration

### Rate Limiting

The system uses Resilience4j for rate limiting different operations:

- **CREATE**: 10 requests per minute (uploads, folder creation)
- **READ**: 100 requests per minute (file access, search)
- **ADMIN**: 20 requests per minute (delete, rename, move, share)

### Storage Configuration

```yaml
# File storage settings
file:
  storage:
    base-path: 'storage/files/'
    max-file-size: 100MB
    allowed-mime-types:
      - 'image/*'
      - 'application/pdf'
      - 'text/*'
      - 'application/json'
```

## Database Schema

### file_storage

- Core file and folder information
- Ownership and permissions
- Storage provider details
- Metadata and access tracking

### file_shares

- File sharing relationships
- Permission levels and access tokens
- Share links and expiration dates
- Access tracking

### file_versions

- File version history
- Version metadata and storage paths
- Change descriptions
- Current version tracking

## Usage Examples

### Upload a File

```bash
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -H "X-User-ID: user123" \
  -F "file=@document.pdf" \
  -F "displayName=Important Document" \
  -F "parentId=1" \
  -F "isPublic=false"
```

### Create a Folder

```bash
curl -X POST "http://localhost:8080/api/v1/files/folders" \
  -H "X-User-ID: user123" \
  -F "name=Work Documents" \
  -F "parentId=1"
```

### Share a File

```bash
curl -X POST "http://localhost:8080/api/v1/files/123/share" \
  -H "X-User-ID: user123" \
  -H "Content-Type: application/json" \
  -d '{
    "sharedWith": "user456",
    "shareType": "USER",
    "permissionLevel": "READ",
    "expiresAt": "2024-12-31T23:59:59"
  }'
```

### Get File Versions

```bash
curl -X GET "http://localhost:8080/api/v1/files/123/versions" \
  -H "X-User-ID: user123"
```

## Security Considerations

1. **Authentication**: All endpoints require user authentication via X-User-ID header
2. **Authorization**: Users can only access their own files or shared files
3. **Rate Limiting**: Prevents abuse and ensures fair usage
4. **File Validation**: Validates file types and sizes
5. **Path Traversal**: Prevents directory traversal attacks
6. **Checksum Verification**: Ensures file integrity

## Performance Optimizations

1. **Database Indexing**: Optimized indexes for common queries
2. **Pagination**: Large result sets are paginated
3. **Caching**: File metadata is cached for faster access
4. **Deduplication**: Prevents storage waste with duplicate detection
5. **Async Operations**: File processing is asynchronous where possible

## Monitoring and Analytics

- File upload/download statistics
- Storage usage per user
- Share link access tracking
- Version history analytics
- Performance metrics

## Future Enhancements

1. **Real-time Collaboration**: Live editing and collaboration features
2. **Advanced Search**: Full-text search and content indexing
3. **File Preview**: Built-in file preview capabilities
4. **Mobile Sync**: Mobile app integration
5. **Advanced Permissions**: Role-based access control
6. **File Encryption**: End-to-end encryption for sensitive files
7. **Backup and Recovery**: Automated backup and disaster recovery
8. **Integration APIs**: Third-party service integrations

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- MapStruct for object mapping
- Resilience4j for rate limiting
- Swagger for API documentation

This file storage system provides a solid foundation for building cloud storage applications with enterprise-grade features and scalability.
