package com.notificationservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.converter.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "notification_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationRule extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority")
    private Integer priority = 0;

    // Time-based conditions
    @ElementCollection
    @CollectionTable(name = "rule_days_of_week", joinColumns = @JoinColumn(name = "rule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> daysOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    // Frequency conditions
    @Column(name = "max_notifications_per_day")
    private Integer maxNotificationsPerDay;

    @Column(name = "min_interval_minutes")
    private Integer minIntervalMinutes;

    // Content conditions
    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "conditions", columnDefinition = "text")
    private JsonNode conditions;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "variables", columnDefinition = "text")
    private JsonNode variables;

    // Action settings
    @Column(name = "action_type", nullable = false)
    private String actionType = "SEND_NOTIFICATION";

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "action_config", columnDefinition = "text")
    private JsonNode actionConfig;

    public enum RuleType {
        TIME_BASED, // Rules based on time of day/week
        FREQUENCY_BASED, // Rules based on notification frequency
        CONTENT_BASED, // Rules based on message content
        COMPOSITE // Combination of multiple rule types
    }

    public enum NotificationType {
        EMAIL, WHATSAPP, SMS, PUSH, MOBILE_BROADCAST
    }
}