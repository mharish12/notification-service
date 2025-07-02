package com.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.entity.NotificationRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class NotificationRuleDto extends BaseAuditableDto {

    private Long id;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @NotBlank(message = "User ID is required")
    private String userId;

    private Long templateId;

    private String templateName;

    @NotNull(message = "Rule type is required")
    private NotificationRule.RuleType ruleType;

    @NotNull(message = "Notification type is required")
    private NotificationRule.NotificationType notificationType;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer priority = 0;

    // Time-based conditions
    private Set<DayOfWeek> daysOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @Builder.Default
    private String timezone = "UTC";

    // Frequency conditions
    private Integer maxNotificationsPerDay;

    private Integer minIntervalMinutes;

    // Content conditions
    private JsonNode conditions;

    private JsonNode variables;

    // Action settings
    @Builder.Default
    private String actionType = "SEND_NOTIFICATION";

    private JsonNode actionConfig;
}