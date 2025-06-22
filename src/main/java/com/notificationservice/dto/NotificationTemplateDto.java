package com.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.entity.NotificationTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDto {

    private Long id;

    @NotBlank(message = "Template name is required")
    private String name;

    @NotNull(message = "Template type is required")
    private NotificationTemplate.NotificationType type;

    private String subject;

    @NotBlank(message = "Template content is required")
    private String content;

    private JsonNode variables;

    private Boolean isActive = true;
}