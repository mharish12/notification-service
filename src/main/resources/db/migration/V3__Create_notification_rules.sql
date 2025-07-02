-- Create notification_rules table
CREATE TABLE notification_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    user_id VARCHAR(255) NOT NULL,
    template_id BIGINT REFERENCES notification_templates(id),
    rule_type VARCHAR(50) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    start_time TIME,
    end_time TIME,
    timezone VARCHAR(50) DEFAULT 'UTC',
    max_notifications_per_day INTEGER,
    min_interval_minutes INTEGER,
    conditions TEXT,
    variables TEXT,
    action_type VARCHAR(100) DEFAULT 'SEND_NOTIFICATION',
    action_config TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
);

-- Create rule_days_of_week table for storing days of week for rules
CREATE TABLE rule_days_of_week (
    rule_id BIGINT NOT NULL REFERENCES notification_rules(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    PRIMARY KEY (rule_id, day_of_week)
);

-- Create indexes for better performance
CREATE INDEX idx_notification_rules_user_id ON notification_rules(user_id);
CREATE INDEX idx_notification_rules_is_active ON notification_rules(is_active);
CREATE INDEX idx_notification_rules_priority ON notification_rules(priority);
CREATE INDEX idx_notification_rules_rule_type ON notification_rules(rule_type);
CREATE INDEX idx_notification_rules_notification_type ON notification_rules(notification_type);
CREATE INDEX idx_notification_rules_template_id ON notification_rules(template_id);
CREATE INDEX idx_notification_rules_action_type ON notification_rules(action_type);
CREATE INDEX idx_notification_rules_user_active_priority ON notification_rules(user_id, is_active, priority DESC);

-- Add unique constraint for rule name per user
CREATE UNIQUE INDEX idx_notification_rules_user_name ON notification_rules(user_id, name) WHERE is_active = TRUE; 