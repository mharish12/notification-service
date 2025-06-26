package com.notificationservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.converter.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "notification_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationTemplate extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "subject")
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "variables", columnDefinition = "text")
    private JsonNode variables;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum NotificationType {
        EMAIL, WHATSAPP
    }
}