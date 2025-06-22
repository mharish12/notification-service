package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.entity.NotificationRequest;
import com.notificationservice.entity.NotificationResponse;
import com.notificationservice.repository.NotificationRequestRepository;
import com.notificationservice.repository.NotificationResponseRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WhatsAppService {

    private final NotificationRequestRepository requestRepository;
    private final NotificationResponseRepository responseRepository;
    private final TemplateService templateService;

    @Value("${notification.whatsapp.twilio.account-sid}")
    private String accountSid;

    @Value("${notification.whatsapp.twilio.auth-token}")
    private String authToken;

    @Value("${notification.whatsapp.twilio.from-number}")
    private String fromNumber;

    public NotificationRequestDto sendWhatsAppMessage(String toNumber, String content) {
        return sendWhatsAppMessage(toNumber, content, null);
    }

    public NotificationRequestDto sendWhatsAppMessage(String toNumber, String content, Map<String, Object> variables) {
        // Initialize Twilio if not already done
        if (Twilio.getRestClient() == null) {
            Twilio.init(accountSid, authToken);
        }

        // Process template if variables are provided
        String processedContent = content;
        if (variables != null && !variables.isEmpty()) {
            processedContent = templateService.processTemplateContent(content, variables);
        }

        // Create notification request
        NotificationRequest request = new NotificationRequest();
        request.setRecipient(toNumber);
        request.setContent(processedContent);
        request.setVariables(convertToJsonNode(variables));
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);

        request = requestRepository.save(request);

        try {
            // Send WhatsApp message via Twilio
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + toNumber),
                    new PhoneNumber("whatsapp:" + fromNumber),
                    processedContent).create();

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.SENT);
            request.setSentAt(LocalDateTime.now());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("SENT");
            response.setProviderResponseId(message.getSid());
            response.setResponseData(convertToJsonNode(Map.of(
                    "sid", message.getSid(),
                    "status", message.getStatus().toString(),
                    "errorCode", message.getErrorCode() != null ? message.getErrorCode().toString() : null,
                    "errorMessage", message.getErrorMessage())));
            responseRepository.save(response);

            log.info("WhatsApp message sent successfully to: {}", toNumber);

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to: {}", toNumber, e);

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.FAILED);
            request.setErrorMessage(e.getMessage());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("FAILED");
            response.setResponseData(convertToJsonNode(Map.of("error", e.getMessage())));
            responseRepository.save(response);
        }

        return convertToDto(request);
    }

    public NotificationRequestDto sendWhatsAppWithTemplate(String templateName, String toNumber,
            Map<String, Object> variables) {
        var template = templateService.getTemplateByName(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateName));

        if (template.getType() != com.notificationservice.entity.NotificationTemplate.NotificationType.WHATSAPP) {
            throw new IllegalArgumentException("Template is not a WhatsApp template: " + templateName);
        }

        String processedContent = templateService.processTemplate(templateName, variables);

        return sendWhatsAppMessage(toNumber, processedContent, variables);
    }

    private JsonNode convertToJsonNode(Map<String, Object> map) {
        if (map == null)
            return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(map);
        } catch (Exception e) {
            log.warn("Failed to convert map to JsonNode", e);
            return null;
        }
    }

    private NotificationRequestDto convertToDto(NotificationRequest request) {
        return new NotificationRequestDto(
                request.getId(),
                request.getTemplate() != null ? request.getTemplate().getId() : null,
                request.getTemplate() != null ? request.getTemplate().getName() : null,
                request.getSender() != null ? request.getSender().getId() : null,
                request.getSender() != null ? request.getSender().getName() : null,
                request.getRecipient(),
                request.getSubject(),
                request.getContent(),
                request.getVariables(),
                request.getStatus().name(),
                request.getErrorMessage());
    }
}