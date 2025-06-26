-- Add audit fields to notification_templates table
ALTER TABLE notification_templates 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

-- Add audit fields to email_senders table
ALTER TABLE email_senders
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

-- Add audit fields to notification_requests table
ALTER TABLE notification_requests
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

-- Add audit fields to notification_responses table
ALTER TABLE notification_responses
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS modified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);
