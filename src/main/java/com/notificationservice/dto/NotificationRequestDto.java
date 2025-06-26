package com.notificationservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationRequestDto extends BaseAuditableDto {

    private Long id;

    private Long templateId;

    private String templateName;

    private Long senderId;

    private String senderName;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private JsonNode variables;

    private String status;

    private String errorMessage;
}