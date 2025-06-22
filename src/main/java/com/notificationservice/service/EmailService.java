package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.entity.EmailSender;
import com.notificationservice.entity.NotificationRequest;
import com.notificationservice.entity.NotificationResponse;
import com.notificationservice.repository.EmailSenderRepository;
import com.notificationservice.repository.NotificationRequestRepository;
import com.notificationservice.repository.NotificationResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailService {

    private final EmailSenderRepository emailSenderRepository;
    private final NotificationRequestRepository requestRepository;
    private final NotificationResponseRepository responseRepository;
    private final TemplateService templateService;

    public NotificationRequestDto sendEmail(String senderName, String recipient, String subject, String content) {
        return sendEmail(senderName, recipient, subject, content, null);
    }

    public NotificationRequestDto sendEmail(String senderName, String recipient, String subject, String content,
            Map<String, Object> variables) {
        EmailSender emailSender = emailSenderRepository.findByNameAndIsActiveTrue(senderName)
                .orElseThrow(() -> new IllegalArgumentException("Email sender not found: " + senderName));

        // Process template if variables are provided
        String processedContent = content;
        if (variables != null && !variables.isEmpty()) {
            processedContent = templateService.processTemplateContent(content, variables);
        }

        // Create notification request
        NotificationRequest request = new NotificationRequest();
        request.setSender(emailSender);
        request.setRecipient(recipient);
        request.setSubject(subject);
        request.setContent(processedContent);
        request.setVariables(convertToJsonNode(variables));
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);

        request = requestRepository.save(request);

        try {
            // Send email
            JavaMailSender mailSender = createMailSender(emailSender);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailSender.getUsername());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(processedContent, true); // true for HTML content

            mailSender.send(message);

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.SENT);
            request.setSentAt(LocalDateTime.now());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("SENT");
            response.setProviderResponseId("email-" + System.currentTimeMillis());
            responseRepository.save(response);

            log.info("Email sent successfully to: {}", recipient);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", recipient, e);

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

    public NotificationRequestDto sendEmailWithTemplate(String senderName, String templateName, String recipient,
            Map<String, Object> variables) {
        var template = templateService.getTemplateByName(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateName));

        if (template.getType() != com.notificationservice.entity.NotificationTemplate.NotificationType.EMAIL) {
            throw new IllegalArgumentException("Template is not an email template: " + templateName);
        }

        String processedContent = templateService.processTemplate(templateName, variables);
        String subject = template.getSubject();

        return sendEmail(senderName, recipient, subject, processedContent, variables);
    }

    private JavaMailSender createMailSender(EmailSender emailSender) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailSender.getHost());
        mailSender.setPort(emailSender.getPort());
        mailSender.setUsername(emailSender.getUsername());
        mailSender.setPassword(emailSender.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        if (emailSender.getProperties() != null) {
            emailSender.getProperties().fieldNames().forEachRemaining(key -> {
                props.put(key, emailSender.getProperties().get(key).asText());
            });
        }

        return mailSender;
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