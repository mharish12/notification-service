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
public class ValidatedEmailRequestDto {

    @NotBlank(message = "Sender name is required and cannot be empty")
    @Size(min = 1, max = 255, message = "Sender name must be between 1 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Sender name can only contain letters, numbers, spaces, hyphens, and underscores")
    private String senderName;

    @NotBlank(message = "Recipient email is required and cannot be empty")
    @Email(message = "Recipient must be a valid email address")
    @Size(max = 255, message = "Recipient email cannot exceed 255 characters")
    private String recipient;

    @NotBlank(message = "Subject is required and cannot be empty")
    @Size(min = 1, max = 255, message = "Subject must be between 1 and 255 characters")
    private String subject;

    @NotBlank(message = "Content is required and cannot be empty")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    @Valid
    private Map<String, Object> variables;

    // Cross-field validation methods
    @AssertTrue(message = "Subject cannot contain spam indicators")
    public boolean isSubjectValid() {
        if (subject != null) {
            String lowerSubject = subject.toLowerCase();
            return !lowerSubject.contains("spam") &&
                    !lowerSubject.contains("urgent") &&
                    !lowerSubject.contains("limited time") &&
                    !lowerSubject.contains("act now");
        }
        return true;
    }

    @AssertTrue(message = "Content cannot contain spam indicators")
    public boolean isContentValid() {
        if (content != null) {
            String lowerContent = content.toLowerCase();
            return !lowerContent.contains("spam") &&
                    !lowerContent.contains("urgent") &&
                    !lowerContent.contains("limited time") &&
                    !lowerContent.contains("act now");
        }
        return true;
    }

    @AssertTrue(message = "Recipient email domain must be valid")
    public boolean isRecipientDomainValid() {
        if (recipient != null && recipient.contains("@")) {
            String domain = recipient.substring(recipient.indexOf("@") + 1);
            return domain.contains(".") && domain.length() > 2;
        }
        return true;
    }
}