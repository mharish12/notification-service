package com.notificationservice.controller;

import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.service.EmailService;
import com.notificationservice.service.RuleEngineService;
import com.notificationservice.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final RuleEngineService ruleEngineService;

    // Email endpoints
    @PostMapping("/email")
    public ResponseEntity<NotificationRequestDto> sendEmail(@RequestBody EmailRequest request) {
        try {
            // Evaluate rules before sending
            String userId = extractUserId(request.getVariables());
            if (userId != null) {
                RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                        userId, request.getContent(), request.getVariables());

                if (result.isBlocked()) {
                    log.warn("Email blocked by rule for user: {}, reason: {}", userId, result.getBlockReason());
                    return ResponseEntity.status(403).build();
                }

                // Update user stats if rules were applied
                if (!result.getAppliedRules().isEmpty()) {
                    ruleEngineService.updateUserStats(userId);
                }
            }

            NotificationRequestDto result = emailService.sendEmail(
                    request.getSenderName(),
                    request.getRecipient(),
                    request.getSubject(),
                    request.getContent(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/email/template/{templateName}")
    public ResponseEntity<NotificationRequestDto> sendEmailWithTemplate(
            @PathVariable String templateName,
            @RequestBody TemplateEmailRequest request) {
        try {
            // Evaluate rules before sending
            String userId = extractUserId(request.getVariables());
            if (userId != null) {
                RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                        userId, "Template: " + templateName, request.getVariables());

                if (result.isBlocked()) {
                    log.warn("Email template blocked by rule for user: {}, reason: {}", userId,
                            result.getBlockReason());
                    return ResponseEntity.status(403).build();
                }

                // Update user stats if rules were applied
                if (!result.getAppliedRules().isEmpty()) {
                    ruleEngineService.updateUserStats(userId);
                }
            }

            NotificationRequestDto result = emailService.sendEmailWithTemplate(
                    request.getSenderName(),
                    templateName,
                    request.getRecipient(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // WhatsApp endpoints
    @PostMapping("/whatsapp")
    public ResponseEntity<NotificationRequestDto> sendWhatsApp(@RequestBody WhatsAppRequest request) {
        try {
            // Evaluate rules before sending
            String userId = extractUserId(request.getVariables());
            if (userId != null) {
                RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                        userId, request.getContent(), request.getVariables());

                if (result.isBlocked()) {
                    log.warn("WhatsApp blocked by rule for user: {}, reason: {}", userId, result.getBlockReason());
                    return ResponseEntity.status(403).build();
                }

                // Update user stats if rules were applied
                if (!result.getAppliedRules().isEmpty()) {
                    ruleEngineService.updateUserStats(userId);
                }
            }

            NotificationRequestDto result = whatsAppService.sendWhatsAppMessage(
                    request.getToNumber(),
                    request.getContent(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/whatsapp/template/{templateName}")
    public ResponseEntity<NotificationRequestDto> sendWhatsAppWithTemplate(
            @PathVariable String templateName,
            @RequestBody TemplateWhatsAppRequest request) {
        try {
            // Evaluate rules before sending
            String userId = extractUserId(request.getVariables());
            if (userId != null) {
                RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                        userId, "Template: " + templateName, request.getVariables());

                if (result.isBlocked()) {
                    log.warn("WhatsApp template blocked by rule for user: {}, reason: {}", userId,
                            result.getBlockReason());
                    return ResponseEntity.status(403).build();
                }

                // Update user stats if rules were applied
                if (!result.getAppliedRules().isEmpty()) {
                    ruleEngineService.updateUserStats(userId);
                }
            }

            NotificationRequestDto result = whatsAppService.sendWhatsAppWithTemplate(
                    templateName,
                    request.getToNumber(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Extract user ID from variables or use recipient as fallback
     */
    private String extractUserId(Map<String, Object> variables) {
        if (variables != null && variables.containsKey("userId")) {
            return variables.get("userId").toString();
        }
        return null;
    }

    // Request classes
    public static class EmailRequest {
        private String senderName;
        private String recipient;
        private String subject;
        private String content;
        private Map<String, Object> variables;

        // Getters and setters
        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getRecipient() {
            return recipient;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class TemplateEmailRequest {
        private String senderName;
        private String recipient;
        private Map<String, Object> variables;

        // Getters and setters
        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getRecipient() {
            return recipient;
        }

        public void setRecipient(String recipient) {
            this.recipient = recipient;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class WhatsAppRequest {
        private String toNumber;
        private String content;
        private Map<String, Object> variables;

        // Getters and setters
        public String getToNumber() {
            return toNumber;
        }

        public void setToNumber(String toNumber) {
            this.toNumber = toNumber;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class TemplateWhatsAppRequest {
        private String toNumber;
        private Map<String, Object> variables;

        // Getters and setters
        public String getToNumber() {
            return toNumber;
        }

        public void setToNumber(String toNumber) {
            this.toNumber = toNumber;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }
}