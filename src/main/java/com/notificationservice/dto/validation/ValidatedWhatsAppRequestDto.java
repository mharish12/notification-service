package com.notificationservice.dto.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatedWhatsAppRequestDto {

    @NotBlank(message = "Phone number is required and cannot be empty")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format (e.g., +1234567890)")
    @Size(min = 8, max = 20, message = "Phone number must be between 8 and 20 characters")
    private String toNumber;

    @NotBlank(message = "Content is required and cannot be empty")
    @Size(min = 1, max = 1600, message = "Content must be between 1 and 1600 characters for WhatsApp")
    private String content;

    @Valid
    private Map<String, Object> variables;

    // Cross-field validation methods
    @AssertTrue(message = "Content cannot contain spam indicators")
    public boolean isContentValid() {
        if (content != null) {
            String lowerContent = content.toLowerCase();
            return !lowerContent.contains("spam") &&
                    !lowerContent.contains("urgent") &&
                    !lowerContent.contains("limited time") &&
                    !lowerContent.contains("act now") &&
                    !lowerContent.contains("click here") &&
                    !lowerContent.contains("unsubscribe");
        }
        return true;
    }

    @AssertTrue(message = "Phone number country code must be valid")
    public boolean isPhoneNumberCountryCodeValid() {
        if (toNumber != null && toNumber.startsWith("+")) {
            String countryCode = toNumber.substring(1, 3);
            // Basic validation for common country codes
            return countryCode.matches("^[1-9]\\d$");
        }
        return true;
    }

    @AssertTrue(message = "Content must not exceed WhatsApp message limits")
    public boolean isContentLengthValid() {
        if (content != null) {
            // WhatsApp has different limits for different message types
            // For text messages, limit is 1600 characters
            return content.length() <= 1600;
        }
        return true;
    }
}