package com.notificationservice.service;

import com.notificationservice.dto.validation.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final Validator validator;

    /**
     * Validate a notification rule DTO
     */
    public ValidationResultDto validateNotificationRule(ValidatedNotificationRuleDto ruleDto) {
        log.debug("Validating notification rule: {}", ruleDto.getName());

        Set<ConstraintViolation<ValidatedNotificationRuleDto>> violations = validator.validate(ruleDto);

        if (violations.isEmpty()) {
            return ValidationResultDto.success("NotificationRule", ruleDto.getName());
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure("NotificationRule", ruleDto.getName(), violationDtos);
    }

    /**
     * Validate an email request DTO
     */
    public ValidationResultDto validateEmailRequest(ValidatedEmailRequestDto emailDto) {
        log.debug("Validating email request for recipient: {}", emailDto.getRecipient());

        Set<ConstraintViolation<ValidatedEmailRequestDto>> violations = validator.validate(emailDto);

        if (violations.isEmpty()) {
            return ValidationResultDto.success("EmailRequest", emailDto.getRecipient());
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure("EmailRequest", emailDto.getRecipient(), violationDtos);
    }

    /**
     * Validate a WhatsApp request DTO
     */
    public ValidationResultDto validateWhatsAppRequest(ValidatedWhatsAppRequestDto whatsAppDto) {
        log.debug("Validating WhatsApp request for number: {}", whatsAppDto.getToNumber());

        Set<ConstraintViolation<ValidatedWhatsAppRequestDto>> violations = validator.validate(whatsAppDto);

        if (violations.isEmpty()) {
            return ValidationResultDto.success("WhatsAppRequest", whatsAppDto.getToNumber());
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure("WhatsAppRequest", whatsAppDto.getToNumber(), violationDtos);
    }

    /**
     * Validate a mobile broadcast request DTO
     */
    public ValidationResultDto validateMobileBroadcastRequest(ValidatedMobileBroadcastRequestDto broadcastDto) {
        log.debug("Validating mobile broadcast request for network: {}", broadcastDto.getNetworkId());

        Set<ConstraintViolation<ValidatedMobileBroadcastRequestDto>> violations = validator.validate(broadcastDto);

        if (violations.isEmpty()) {
            return ValidationResultDto.success("MobileBroadcastRequest", broadcastDto.getNetworkId());
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure("MobileBroadcastRequest", broadcastDto.getNetworkId(), violationDtos);
    }

    /**
     * Validate any object with Hibernate validation annotations
     */
    public <T> ValidationResultDto validateObject(T object, String objectType, String objectId) {
        log.debug("Validating {}: {}", objectType, objectId);

        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType, objectId, violationDtos);
    }

    /**
     * Validate object with custom validation groups
     */
    public <T> ValidationResultDto validateObjectWithGroups(T object, String objectType, String objectId,
            Class<?>... groups) {
        log.debug("Validating {}: {} with groups: {}", objectType, objectId, groups);

        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType, objectId, violationDtos);
    }

    /**
     * Validate a property of an object
     */
    public <T> ValidationResultDto validateProperty(T object, String propertyName, String objectType, String objectId) {
        log.debug("Validating property {} of {}: {}", propertyName, objectType, objectId);

        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType + "." + propertyName, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType + "." + propertyName, objectId, violationDtos);
    }

    /**
     * Validate a property with custom validation groups
     */
    public <T> ValidationResultDto validatePropertyWithGroups(T object, String propertyName, String objectType,
            String objectId, Class<?>... groups) {
        log.debug("Validating property {} of {}: {} with groups: {}", propertyName, objectType, objectId, groups);

        Set<ConstraintViolation<T>> violations = validator.validateProperty(object, propertyName, groups);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType + "." + propertyName, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType + "." + propertyName, objectId, violationDtos);
    }

    /**
     * Validate a value against a specific constraint
     */
    public ValidationResultDto validateValue(Class<?> beanType, String propertyName, Object value, String objectType,
            String objectId) {
        log.debug("Validating value for property {} of {}: {}", propertyName, objectType, objectId);

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set<?>) validator
                .validateValue(beanType, propertyName, value);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType + "." + propertyName, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType + "." + propertyName, objectId, violationDtos);
    }

    /**
     * Validate a value with custom validation groups
     */
    public ValidationResultDto validateValueWithGroups(Class<?> beanType, String propertyName, Object value,
            String objectType, String objectId, Class<?>... groups) {
        log.debug("Validating value for property {} of {}: {} with groups: {}", propertyName, objectType, objectId,
                groups);

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> violations = (Set<ConstraintViolation<?>>) (Set<?>) validator
                .validateValue(beanType, propertyName, value, groups);

        if (violations.isEmpty()) {
            return ValidationResultDto.success(objectType + "." + propertyName, objectId);
        }

        List<ValidationViolationDto> violationDtos = violations.stream()
                .map(this::convertToViolationDto)
                .collect(Collectors.toList());

        return ValidationResultDto.failure(objectType + "." + propertyName, objectId, violationDtos);
    }

    /**
     * Convert Hibernate ConstraintViolation to ValidationViolationDto
     */
    private <T> ValidationViolationDto convertToViolationDto(ConstraintViolation<T> violation) {
        return ValidationViolationDto.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .invalidValue(violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null)
                .constraintType(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .path(violation.getPropertyPath().toString())
                .build();
    }

    /**
     * Get validation summary
     */
    public ValidationSummaryDto getValidationSummary(List<ValidationResultDto> results) {
        long totalValidations = results.size();
        long validCount = results.stream().mapToLong(r -> r.isValid() ? 1 : 0).sum();
        long invalidCount = totalValidations - validCount;

        long totalViolations = results.stream()
                .mapToLong(ValidationResultDto::getViolationCount)
                .sum();

        return ValidationSummaryDto.builder()
                .totalValidations(totalValidations)
                .validCount(validCount)
                .invalidCount(invalidCount)
                .totalViolations(totalViolations)
                .successRate(validCount > 0 ? (double) validCount / totalValidations : 0.0)
                .build();
    }

    /**
     * Check if validation result is valid
     */
    public boolean isValid(ValidationResultDto result) {
        return result != null && result.isValid();
    }

    /**
     * Check if any validation result in a list is invalid
     */
    public boolean hasAnyInvalidation(List<ValidationResultDto> results) {
        return results.stream().anyMatch(result -> !result.isValid());
    }

    /**
     * Get all violations from a list of validation results
     */
    public List<ValidationViolationDto> getAllViolations(List<ValidationResultDto> results) {
        return results.stream()
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getViolations().stream())
                .collect(Collectors.toList());
    }
}