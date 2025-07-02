package com.notificationservice.controller;

import com.notificationservice.dto.validation.*;
import com.notificationservice.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {

    private final ValidationService validationService;

    /**
     * Validate a notification rule
     */
    @PostMapping("/rules")
    public ResponseEntity<ValidationResultDto> validateNotificationRule(
            @RequestBody ValidatedNotificationRuleDto ruleDto) {
        log.info("Validating notification rule: {}", ruleDto.getName());
        ValidationResultDto result = validationService.validateNotificationRule(ruleDto);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Validate an email request
     */
    @PostMapping("/email")
    public ResponseEntity<ValidationResultDto> validateEmailRequest(@RequestBody ValidatedEmailRequestDto emailDto) {
        log.info("Validating email request for recipient: {}", emailDto.getRecipient());
        ValidationResultDto result = validationService.validateEmailRequest(emailDto);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Validate a WhatsApp request
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<ValidationResultDto> validateWhatsAppRequest(
            @RequestBody ValidatedWhatsAppRequestDto whatsAppDto) {
        log.info("Validating WhatsApp request for number: {}", whatsAppDto.getToNumber());
        ValidationResultDto result = validationService.validateWhatsAppRequest(whatsAppDto);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Validate a mobile broadcast request
     */
    @PostMapping("/mobile-broadcast")
    public ResponseEntity<ValidationResultDto> validateMobileBroadcastRequest(
            @RequestBody ValidatedMobileBroadcastRequestDto broadcastDto) {
        log.info("Validating mobile broadcast request for network: {}", broadcastDto.getNetworkId());
        ValidationResultDto result = validationService.validateMobileBroadcastRequest(broadcastDto);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Validate any object (generic validation)
     */
    @PostMapping("/object")
    public ResponseEntity<ValidationResultDto> validateObject(@RequestBody ValidationRequest request) {
        log.info("Validating object of type: {}", request.getObjectType());

        try {
            // This is a simplified example - in a real implementation, you'd need to
            // deserialize the object
            // based on the objectType parameter
            ValidationResultDto result = ValidationResultDto.failure(
                    request.getObjectType(),
                    request.getObjectId(),
                    List.of(ValidationViolationDto.of("object", "Generic object validation not implemented", "N/A")));

            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("Error validating object", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate a specific property
     */
    @PostMapping("/property")
    public ResponseEntity<ValidationResultDto> validateProperty(@RequestBody PropertyValidationRequest request) {
        log.info("Validating property {} of type: {}", request.getPropertyName(), request.getObjectType());

        try {
            // This is a simplified example - in a real implementation, you'd need to create
            // the object
            // and validate the specific property
            ValidationResultDto result = ValidationResultDto.failure(
                    request.getObjectType() + "." + request.getPropertyName(),
                    request.getObjectId(),
                    List.of(ValidationViolationDto.of("property", "Property validation not implemented", "N/A")));

            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("Error validating property", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate a value against a specific constraint
     */
    @PostMapping("/value")
    public ResponseEntity<ValidationResultDto> validateValue(@RequestBody ValueValidationRequest request) {
        log.info("Validating value for property {} of type: {}", request.getPropertyName(), request.getObjectType());

        try {
            ValidationResultDto result = validationService.validateValue(
                    request.getBeanType(),
                    request.getPropertyName(),
                    request.getValue(),
                    request.getObjectType(),
                    request.getObjectId());

            if (result.isValid()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Error validating value", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get validation summary for multiple results
     */
    @PostMapping("/summary")
    public ResponseEntity<ValidationSummaryDto> getValidationSummary(@RequestBody List<ValidationResultDto> results) {
        log.info("Getting validation summary for {} results", results.size());

        ValidationSummaryDto summary = validationService.getValidationSummary(results);
        return ResponseEntity.ok(summary);
    }

    /**
     * Check if validation result is valid
     */
    @PostMapping("/check-valid")
    public ResponseEntity<Map<String, Object>> checkValidationResult(@RequestBody ValidationResultDto result) {
        boolean isValid = validationService.isValid(result);

        Map<String, Object> response = Map.of(
                "valid", isValid,
                "violationCount", result.getViolationCount(),
                "hasViolations", result.hasViolations());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all violations from multiple validation results
     */
    @PostMapping("/violations")
    public ResponseEntity<List<ValidationViolationDto>> getAllViolations(
            @RequestBody List<ValidationResultDto> results) {
        log.info("Getting all violations from {} validation results", results.size());

        List<ValidationViolationDto> violations = validationService.getAllViolations(results);
        return ResponseEntity.ok(violations);
    }

    // Request classes

    public static class ValidationRequest {
        private String objectType;
        private String objectId;
        private Object object;

        // Getters and setters
        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

    public static class PropertyValidationRequest {
        private String objectType;
        private String objectId;
        private String propertyName;
        private Object object;

        // Getters and setters
        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

    public static class ValueValidationRequest {
        private Class<?> beanType;
        private String propertyName;
        private Object value;
        private String objectType;
        private String objectId;

        // Getters and setters
        public Class<?> getBeanType() {
            return beanType;
        }

        public void setBeanType(Class<?> beanType) {
            this.beanType = beanType;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }
    }
}