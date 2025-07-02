package com.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.entity.NotificationTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class NotificationTemplateDto extends BaseAuditableDto {

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