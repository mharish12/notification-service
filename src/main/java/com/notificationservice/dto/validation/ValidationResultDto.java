package com.notificationservice.dto.validation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResultDto {

    private boolean valid;
    private String objectType;
    private String objectId;
    private LocalDateTime validationTime;
    private List<ValidationViolationDto> violations;
    private Map<String, Object> additionalInfo;

    public static ValidationResultDto success(String objectType, String objectId) {
        return ValidationResultDto.builder()
                .valid(true)
                .objectType(objectType)
                .objectId(objectId)
                .validationTime(LocalDateTime.now())
                .build();
    }

    public static ValidationResultDto failure(String objectType, String objectId,
            List<ValidationViolationDto> violations) {
        return ValidationResultDto.builder()
                .valid(false)
                .objectType(objectType)
                .objectId(objectId)
                .validationTime(LocalDateTime.now())
                .violations(violations)
                .build();
    }

    public void addViolation(ValidationViolationDto violation) {
        if (violations == null) {
            violations = new java.util.ArrayList<>();
        }
        violations.add(violation);
        this.valid = false;
    }

    public void addViolation(String field, String message, String invalidValue) {
        addViolation(ValidationViolationDto.builder()
                .field(field)
                .message(message)
                .invalidValue(invalidValue)
                .build());
    }

    public boolean hasViolations() {
        return violations != null && !violations.isEmpty();
    }

    public int getViolationCount() {
        return violations != null ? violations.size() : 0;
    }
}