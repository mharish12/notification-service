-- Create notification_templates table
CREATE TABLE notification_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'WHATSAPP')),
    subject VARCHAR(500),
    content TEXT NOT NULL,
    variables JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create email_senders table
CREATE TABLE email_senders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    properties JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_requests table
CREATE TABLE notification_requests (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT REFERENCES notification_templates(id),
    sender_id BIGINT REFERENCES email_senders(id),
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    content TEXT NOT NULL,
    variables JSONB,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_responses table
CREATE TABLE notification_responses (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES notification_requests(id),
    provider_response_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    response_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default email senders
INSERT INTO email_senders (name, host, port, username, password, properties) VALUES
('gmail', 'smtp.gmail.com', 587, 'default@gmail.com', 'default-password', '{"mail.smtp.auth": "true", "mail.smtp.starttls.enable": "true"}'),
('outlook', 'smtp-mail.outlook.com', 587, 'default@outlook.com', 'default-password', '{"mail.smtp.auth": "true", "mail.smtp.starttls.enable": "true"}');

-- Insert sample templates
INSERT INTO notification_templates (name, type, subject, content, variables) VALUES
('welcome-email', 'EMAIL', 'Welcome to Our Service', 'Hello {{name}}, Welcome to our service! Your account has been created successfully.', '["name"]'),
('password-reset', 'EMAIL', 'Password Reset Request', 'Hello {{name}}, You requested a password reset. Click here: {{resetLink}}', '["name", "resetLink"]'),
('order-confirmation', 'EMAIL', 'Order Confirmation', 'Hello {{name}}, Your order #{{orderId}} has been confirmed. Total: ${{amount}}', '["name", "orderId", "amount"]'),
('whatsapp-welcome', 'WHATSAPP', NULL, 'Hello {{name}}! Welcome to our service. Your account has been created successfully.', '["name"]'),
('whatsapp-otp', 'WHATSAPP', NULL, 'Your OTP is: {{otp}}. Valid for 5 minutes.', '["otp"]');

-- Create indexes for better performance
CREATE INDEX idx_notification_templates_type ON notification_templates(type);
CREATE INDEX idx_notification_templates_active ON notification_templates(is_active);
CREATE INDEX idx_notification_requests_status ON notification_requests(status);
CREATE INDEX idx_notification_requests_created_at ON notification_requests(created_at);
CREATE INDEX idx_email_senders_active ON email_senders(is_active); 