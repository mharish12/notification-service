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
public class ValidatedMobileBroadcastRequestDto {

    @NotBlank(message = "Network ID is required and cannot be empty")
    @Size(min = 1, max = 100, message = "Network ID must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Network ID can only contain letters, numbers, hyphens, and underscores")
    private String networkId;

    @NotBlank(message = "Content is required and cannot be empty")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters for mobile broadcast")
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

    @AssertTrue(message = "Network ID must not be a reserved keyword")
    public boolean isNetworkIdValid() {
        if (networkId != null) {
            String lowerNetworkId = networkId.toLowerCase();
            return !lowerNetworkId.equals("default") &&
                    !lowerNetworkId.equals("system") &&
                    !lowerNetworkId.equals("admin") &&
                    !lowerNetworkId.equals("root");
        }
        return true;
    }

    @AssertTrue(message = "Content must not exceed mobile broadcast limits")
    public boolean isContentLengthValid() {
        if (content != null) {
            // Mobile broadcast has different limits
            return content.length() <= 5000;
        }
        return true;
    }
}