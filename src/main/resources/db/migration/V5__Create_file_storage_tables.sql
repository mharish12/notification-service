-- Create file storage table
CREATE TABLE file_storage (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(500),
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(255),
    file_extension VARCHAR(50),
    is_folder BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    parent_id BIGINT,
    owner_id VARCHAR(255) NOT NULL,
    storage_provider VARCHAR(50) DEFAULT 'LOCAL',
    storage_path VARCHAR(1000),
    checksum VARCHAR(64),
    version INTEGER DEFAULT 1,
    last_accessed TIMESTAMP,
    thumbnail_path VARCHAR(1000),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_file_storage_parent FOREIGN KEY (parent_id) REFERENCES file_storage(id),
    CONSTRAINT uk_file_storage_path_owner UNIQUE (file_path, owner_id, is_deleted)
);

-- Create file shares table
CREATE TABLE file_shares (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    shared_by VARCHAR(255) NOT NULL,
    shared_with VARCHAR(255),
    share_type VARCHAR(50) NOT NULL,
    permission_level VARCHAR(50) NOT NULL,
    share_link VARCHAR(500),
    access_token VARCHAR(255),
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    download_count INTEGER DEFAULT 0,
    last_accessed TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_file_shares_file FOREIGN KEY (file_id) REFERENCES file_storage(id),
    CONSTRAINT uk_file_shares_token UNIQUE (access_token),
    CONSTRAINT uk_file_shares_link UNIQUE (share_link)
);

-- Create file versions table
CREATE TABLE file_versions (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    version_number INTEGER NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    checksum VARCHAR(64) NOT NULL,
    mime_type VARCHAR(255),
    storage_provider VARCHAR(50),
    storage_path VARCHAR(1000),
    change_description TEXT,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_file_versions_file FOREIGN KEY (file_id) REFERENCES file_storage(id),
    CONSTRAINT uk_file_versions_file_version UNIQUE (file_id, version_number)
);

-- Create indexes for better performance
CREATE INDEX idx_file_storage_owner ON file_storage(owner_id, is_deleted);
CREATE INDEX idx_file_storage_parent ON file_storage(parent_id, is_deleted);
CREATE INDEX idx_file_storage_checksum ON file_storage(checksum, owner_id, is_deleted);
CREATE INDEX idx_file_storage_shared ON file_storage(is_shared, is_deleted);
CREATE INDEX idx_file_storage_public ON file_storage(is_public, is_deleted);
CREATE INDEX idx_file_storage_last_accessed ON file_storage(last_accessed DESC);

CREATE INDEX idx_file_shares_file ON file_shares(file_id, is_active);
CREATE INDEX idx_file_shares_shared_with ON file_shares(shared_with, is_active);
CREATE INDEX idx_file_shares_shared_by ON file_shares(shared_by, is_active);
CREATE INDEX idx_file_shares_expires ON file_shares(expires_at, is_active);

CREATE INDEX idx_file_versions_file ON file_versions(file_id);
CREATE INDEX idx_file_versions_current ON file_versions(file_id, is_current);
CREATE INDEX idx_file_versions_checksum ON file_versions(checksum);

-- Add comments for documentation
COMMENT ON TABLE file_storage IS 'Main table for storing file and folder information';
COMMENT ON TABLE file_shares IS 'Table for managing file sharing permissions and links';
COMMENT ON TABLE file_versions IS 'Table for storing file version history';

COMMENT ON COLUMN file_storage.name IS 'Internal file name (unique identifier)';
COMMENT ON COLUMN file_storage.display_name IS 'User-friendly display name';
COMMENT ON COLUMN file_storage.file_path IS 'Logical path in the file system';
COMMENT ON COLUMN file_storage.storage_path IS 'Physical path on storage provider';
COMMENT ON COLUMN file_storage.checksum IS 'SHA-256 hash for deduplication';
COMMENT ON COLUMN file_storage.metadata IS 'JSON string for additional metadata';

COMMENT ON COLUMN file_shares.share_type IS 'Type of sharing: USER, LINK, TEAM, PUBLIC';
COMMENT ON COLUMN file_shares.permission_level IS 'Permission level: READ, WRITE, ADMIN';
COMMENT ON COLUMN file_shares.access_token IS 'Unique token for accessing shared files';
COMMENT ON COLUMN file_shares.share_link IS 'Public URL for accessing shared files';

COMMENT ON COLUMN file_versions.version_number IS 'Sequential version number';
COMMENT ON COLUMN file_versions.is_current IS 'Indicates if this is the current version';
COMMENT ON COLUMN file_versions.change_description IS 'Description of changes in this version'; 