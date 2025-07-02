package com.notificationservice.dto.validation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationViolationDto {

    private String field;
    private String message;
    private String invalidValue;
    private String constraintType;
    private String constraintValue;
    private String path;

    public static ValidationViolationDto of(String field, String message, String invalidValue) {
        return ValidationViolationDto.builder()
                .field(field)
                .message(message)
                .invalidValue(invalidValue)
                .build();
    }

    public static ValidationViolationDto of(String field, String message, String invalidValue, String constraintType) {
        return ValidationViolationDto.builder()
                .field(field)
                .message(message)
                .invalidValue(invalidValue)
                .constraintType(constraintType)
                .build();
    }

    public static ValidationViolationDto of(String field, String message, String invalidValue, String constraintType,
            String constraintValue) {
        return ValidationViolationDto.builder()
                .field(field)
                .message(message)
                .invalidValue(invalidValue)
                .constraintType(constraintType)
                .constraintValue(constraintValue)
                .build();
    }
}