-- Create notification_templates table
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'WHATSAPP')),
    subject VARCHAR(500),
    content TEXT NOT NULL,
    variables text,
    is_active BOOLEAN DEFAULT true
);

-- Create email_senders table
CREATE TABLE IF NOT EXISTS email_senders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    properties text,
    is_active BOOLEAN DEFAULT true
);

-- Create notification_requests table
CREATE TABLE IF NOT EXISTS notification_requests (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT REFERENCES notification_templates(id),
    sender_id BIGINT REFERENCES email_senders(id),
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    variables text,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    error_message TEXT,
    sent_at TIMESTAMP
);

-- Create notification_responses table
CREATE TABLE IF NOT EXISTS notification_responses (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES notification_requests(id),
    provider_response_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    response_data text
);

-- Insert default email senders
INSERT INTO email_senders (name, host, port, username, password, properties) VALUES
('gmail', 'smtp.gmail.com', 587, 'default@gmail.com', 'default-password', '{"mail.smtp.auth": "true", "mail.smtp.starttls.enable": "true"}'),
('outlook', 'smtp-mail.outlook.com', 587, 'default@outlook.com', 'default-password', '{"mail.smtp.auth": "true", "mail.smtp.starttls.enable": "true"}');

-- Insert sample templates
INSERT INTO
    notification_templates (name, type, subject, content, variables)
VALUES
    ('welcome-email', 'EMAIL', 'Welcome to Our Service', 'Hello {{name}}, Welcome to our service! Your account has been created successfully.', '["name"]'),
    ('password-reset', 'EMAIL', 'Password Reset Request', 'Hello {{name}}, You requested a password reset. Click here: {{resetLink}}', '["name", "resetLink"]'),
    ('order-confirmation', 'EMAIL', 'Order Confirmation', 'Hello {{name}}, Your order #{{orderId}} has been confirmed. Total: {{amount}}', '["name", "orderId", "amount"]'),
    ('whatsapp-welcome', 'WHATSAPP', NULL, 'Hello {{name}}! Welcome to our service. Your account has been created successfully.', '["name"]'),
    ('whatsapp-otp', 'WHATSAPP', NULL, 'Your OTP is: {{otp}}. Valid for 5 minutes.', '["otp"]');

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_notification_templates_type ON notification_templates(type);
CREATE INDEX IF NOT EXISTS idx_notification_templates_active ON notification_templates(is_active);
CREATE INDEX IF NOT EXISTS idx_notification_requests_status ON notification_requests(status);
CREATE INDEX IF NOT EXISTS idx_notification_requests_created_at ON notification_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_email_senders_active ON email_senders(is_active);