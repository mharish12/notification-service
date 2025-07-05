-- Create URL shortener table
CREATE TABLE url_shorteners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(25) NOT NULL UNIQUE,
    custom_alias VARCHAR(50),
    title VARCHAR(255),
    description VARCHAR(500),
    expires_at TIMESTAMP,
    click_count BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    password VARCHAR(255),
    last_accessed_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modified_by VARCHAR(100)
);

-- Create indexes for better performance
CREATE INDEX idx_short_code ON url_shorteners(short_code);
CREATE INDEX idx_original_url ON url_shorteners(original_url);
CREATE INDEX idx_created_at ON url_shorteners(created_at);
CREATE INDEX idx_expires_at ON url_shorteners(expires_at);
CREATE INDEX idx_is_active ON url_shorteners(is_active);
CREATE INDEX idx_click_count ON url_shorteners(click_count);
CREATE INDEX idx_custom_alias ON url_shorteners(custom_alias);

-- Add comments for documentation
ALTER TABLE url_shorteners COMMENT = 'Stores URL shortener information with tracking and analytics data'; 