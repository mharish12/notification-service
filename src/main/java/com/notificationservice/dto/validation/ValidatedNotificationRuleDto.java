package com.notificationservice.dto.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.entity.NotificationRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatedNotificationRuleDto {

    private Long id;

    @NotBlank(message = "Rule name is required and cannot be empty")
    @Size(min = 1, max = 255, message = "Rule name must be between 1 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Rule name can only contain letters, numbers, spaces, hyphens, and underscores")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "User ID is required and cannot be empty")
    @Size(min = 1, max = 255, message = "User ID must be between 1 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "User ID can only contain letters, numbers, hyphens, and underscores")
    private String userId;

    @Min(value = 1, message = "Template ID must be a positive number")
    private Long templateId;

    private String templateName;

    @NotNull(message = "Rule type is required")
    private NotificationRule.RuleType ruleType;

    @NotNull(message = "Notification type is required")
    private NotificationRule.NotificationType notificationType;

    @NotNull(message = "Active status is required")
    private Boolean isActive = true;

    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority = 0;

    // Time-based conditions
    @Valid
    @Size(max = 7, message = "Cannot specify more than 7 days of week")
    private Set<DayOfWeek> daysOfWeek;

    @Valid
    private LocalTime startTime;

    @Valid
    private LocalTime endTime;

    @Size(max = 50, message = "Timezone cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Timezone must be in format 'Region/City' (e.g., 'America/New_York')")
    private String timezone = "UTC";

    // Frequency conditions
    @Min(value = 1, message = "Maximum notifications per day must be at least 1")
    @Max(value = 1000, message = "Maximum notifications per day cannot exceed 1000")
    private Integer maxNotificationsPerDay;

    @Min(value = 1, message = "Minimum interval minutes must be at least 1")
    @Max(value = 1440, message = "Minimum interval minutes cannot exceed 1440 (24 hours)")
    private Integer minIntervalMinutes;

    // Content conditions
    private JsonNode conditions;

    private JsonNode variables;

    // Action settings
    @NotBlank(message = "Action type is required")
    @Size(max = 100, message = "Action type cannot exceed 100 characters")
    @Pattern(regexp = "^(SEND_NOTIFICATION|BLOCK|MODIFY)$", message = "Action type must be one of: SEND_NOTIFICATION, BLOCK, MODIFY")
    private String actionType = "SEND_NOTIFICATION";

    private JsonNode actionConfig;

    // Cross-field validation methods
    @AssertTrue(message = "End time must be after start time when both are specified")
    public boolean isTimeRangeValid() {
        if (startTime != null && endTime != null) {
            return endTime.isAfter(startTime);
        }
        return true;
    }

    @AssertTrue(message = "Time-based rules must specify either days of week or time range")
    public boolean isTimeBasedRuleValid() {
        if (ruleType == NotificationRule.RuleType.TIME_BASED) {
            return (daysOfWeek != null && !daysOfWeek.isEmpty()) ||
                    (startTime != null || endTime != null);
        }
        return true;
    }

    @AssertTrue(message = "Frequency-based rules must specify either max notifications per day or minimum interval")
    public boolean isFrequencyBasedRuleValid() {
        if (ruleType == NotificationRule.RuleType.FREQUENCY_BASED) {
            return maxNotificationsPerDay != null || minIntervalMinutes != null;
        }
        return true;
    }

    @AssertTrue(message = "Content-based rules must specify conditions")
    public boolean isContentBasedRuleValid() {
        if (ruleType == NotificationRule.RuleType.CONTENT_BASED) {
            return conditions != null;
        }
        return true;
    }

    @AssertTrue(message = "Composite rules must specify conditions")
    public boolean isCompositeRuleValid() {
        if (ruleType == NotificationRule.RuleType.COMPOSITE) {
            return conditions != null;
        }
        return true;
    }
}