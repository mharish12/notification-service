# Hibernate Validation Framework

The Hibernate Validation Framework provides comprehensive validation capabilities for DTOs using Hibernate validation annotations. This framework allows developers to create validated DTOs with annotations and get detailed violation information.

## Features

- **Hibernate Validation Annotations**: Full support for all Hibernate validation annotations
- **Custom Validation Logic**: Cross-field validation using `@AssertTrue` methods
- **Detailed Violation Information**: Comprehensive violation details including field, message, and invalid value
- **Validation Service**: Centralized validation service with multiple validation methods
- **REST API**: Validation endpoints for testing and integration
- **Validation Summary**: Statistical information about validation results
- **Type-Safe Validation**: Generic validation methods for any object

## Validated DTOs

### 1. ValidatedNotificationRuleDto

Comprehensive validation for notification rules with cross-field validation.

**Validation Rules:**

- Rule name: Required, 1-255 characters, alphanumeric with spaces/hyphens/underscores
- User ID: Required, 1-255 characters, alphanumeric with hyphens/underscores
- Template ID: Positive number if provided
- Rule type: Required enum value
- Notification type: Required enum value
- Priority: 0-100 range
- Days of week: Maximum 7 days
- Timezone: Format validation (Region/City)
- Frequency limits: 1-1000 notifications per day, 1-1440 minutes interval
- Action type: Must be one of SEND_NOTIFICATION, BLOCK, MODIFY

**Cross-field Validations:**

- End time must be after start time
- Time-based rules must specify days or time range
- Frequency-based rules must specify limits
- Content-based rules must specify conditions
- Composite rules must specify conditions

### 2. ValidatedEmailRequestDto

Email-specific validation with spam detection.

**Validation Rules:**

- Sender name: Required, 1-255 characters, alphanumeric with spaces/hyphens/underscores
- Recipient: Required, valid email format, max 255 characters
- Subject: Required, 1-255 characters
- Content: Required, 1-10000 characters

**Cross-field Validations:**

- Subject cannot contain spam indicators
- Content cannot contain spam indicators
- Recipient email domain must be valid

### 3. ValidatedWhatsAppRequestDto

WhatsApp-specific validation with phone number and content validation.

**Validation Rules:**

- Phone number: Required, international format (+1234567890), 8-20 characters
- Content: Required, 1-1600 characters

**Cross-field Validations:**

- Content cannot contain spam indicators
- Phone number country code must be valid
- Content must not exceed WhatsApp limits

### 4. ValidatedMobileBroadcastRequestDto

Mobile broadcast validation with network and content validation.

**Validation Rules:**

- Network ID: Required, 1-100 characters, alphanumeric with hyphens/underscores
- Content: Required, 1-5000 characters

**Cross-field Validations:**

- Content cannot contain spam indicators
- Network ID must not be reserved keyword
- Content must not exceed broadcast limits

## Validation Service

The `ValidationService` provides comprehensive validation capabilities:

### Basic Validation Methods

```java
// Validate notification rule
ValidationResultDto result = validationService.validateNotificationRule(ruleDto);

// Validate email request
ValidationResultDto result = validationService.validateEmailRequest(emailDto);

// Validate WhatsApp request
ValidationResultDto result = validationService.validateWhatsAppRequest(whatsAppDto);

// Validate mobile broadcast request
ValidationResultDto result = validationService.validateMobileBroadcastRequest(broadcastDto);
```

### Generic Validation Methods

```java
// Validate any object
ValidationResultDto result = validationService.validateObject(object, "ObjectType", "objectId");

// Validate with custom groups
ValidationResultDto result = validationService.validateObjectWithGroups(object, "ObjectType", "objectId", Create.class, Update.class);

// Validate specific property
ValidationResultDto result = validationService.validateProperty(object, "propertyName", "ObjectType", "objectId");

// Validate property with groups
ValidationResultDto result = validationService.validatePropertyWithGroups(object, "propertyName", "ObjectType", "objectId", Create.class);

// Validate value against constraint
ValidationResultDto result = validationService.validateValue(BeanType.class, "propertyName", value, "ObjectType", "objectId");
```

### Utility Methods

```java
// Check if validation result is valid
boolean isValid = validationService.isValid(result);

// Check if any result in list is invalid
boolean hasInvalidation = validationService.hasAnyInvalidation(results);

// Get all violations from multiple results
List<ValidationViolationDto> violations = validationService.getAllViolations(results);

// Get validation summary
ValidationSummaryDto summary = validationService.getValidationSummary(results);
```

## REST API Endpoints

### Validation Endpoints

#### Validate Notification Rule

```http
POST /api/validation/rules
Content-Type: application/json

{
  "name": "My Rule",
  "userId": "user123",
  "ruleType": "TIME_BASED",
  "notificationType": "EMAIL",
  "priority": 1,
  "daysOfWeek": ["MONDAY", "TUESDAY"],
  "startTime": "09:00:00",
  "endTime": "17:00:00"
}
```

#### Validate Email Request

```http
POST /api/validation/email
Content-Type: application/json

{
  "senderName": "system",
  "recipient": "user@example.com",
  "subject": "Important Update",
  "content": "Your account has been updated"
}
```

#### Validate WhatsApp Request

```http
POST /api/validation/whatsapp
Content-Type: application/json

{
  "toNumber": "+1234567890",
  "content": "Your order is ready"
}
```

#### Validate Mobile Broadcast Request

```http
POST /api/validation/mobile-broadcast
Content-Type: application/json

{
  "networkId": "network123",
  "content": "System maintenance scheduled"
}
```

### Utility Endpoints

#### Get Validation Summary

```http
POST /api/validation/summary
Content-Type: application/json

[
  {
    "valid": true,
    "objectType": "EmailRequest",
    "objectId": "user@example.com"
  },
  {
    "valid": false,
    "objectType": "WhatsAppRequest",
    "objectId": "+1234567890",
    "violations": [...]
  }
]
```

#### Check Validation Result

```http
POST /api/validation/check-valid
Content-Type: application/json

{
  "valid": false,
  "objectType": "EmailRequest",
  "objectId": "user@example.com",
  "violations": [...]
}
```

#### Get All Violations

```http
POST /api/validation/violations
Content-Type: application/json

[
  {
    "valid": false,
    "objectType": "EmailRequest",
    "objectId": "user@example.com",
    "violations": [...]
  }
]
```

## Validation Result Structure

### Success Response

```json
{
  "valid": true,
  "objectType": "EmailRequest",
  "objectId": "user@example.com",
  "validationTime": "2024-01-15T10:30:00",
  "violations": null,
  "additionalInfo": null
}
```

### Failure Response

```json
{
  "valid": false,
  "objectType": "EmailRequest",
  "objectId": "user@example.com",
  "validationTime": "2024-01-15T10:30:00",
  "violations": [
    {
      "field": "recipient",
      "message": "Recipient must be a valid email address",
      "invalidValue": "invalid-email",
      "constraintType": "Email",
      "constraintValue": null,
      "path": "recipient"
    },
    {
      "field": "subject",
      "message": "Subject cannot contain spam indicators",
      "invalidValue": "URGENT: Limited time offer",
      "constraintType": "AssertTrue",
      "constraintValue": null,
      "path": "subject"
    }
  ],
  "additionalInfo": null
}
```

## Validation Summary Structure

```json
{
  "totalValidations": 10,
  "validCount": 7,
  "invalidCount": 3,
  "totalViolations": 5,
  "successRate": 0.7,
  "summaryTime": "2024-01-15T10:30:00"
}
```

## Usage Examples

### 1. Creating a Validated DTO

```java
ValidatedNotificationRuleDto ruleDto = ValidatedNotificationRuleDto.builder()
    .name("Business Hours Rule")
    .userId("user123")
    .ruleType(NotificationRule.RuleType.TIME_BASED)
    .notificationType(NotificationRule.NotificationType.EMAIL)
    .priority(1)
    .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
    .startTime(LocalTime.of(9, 0))
    .endTime(LocalTime.of(17, 0))
    .timezone("America/New_York")
    .actionType("SEND_NOTIFICATION")
    .build();
```

### 2. Validating a DTO

```java
ValidationResultDto result = validationService.validateNotificationRule(ruleDto);

if (result.isValid()) {
    // Proceed with business logic
    ruleManagementService.createRule(ruleDto);
} else {
    // Handle validation errors
    log.error("Validation failed: {}", result.getViolations());
    // Return error response to client
}
```

### 3. Handling Validation Errors

```java
if (!result.isValid()) {
    List<ValidationViolationDto> violations = result.getViolations();

    for (ValidationViolationDto violation : violations) {
        log.error("Field: {}, Message: {}, Invalid Value: {}",
            violation.getField(),
            violation.getMessage(),
            violation.getInvalidValue());
    }

    // Create error response
    Map<String, Object> errorResponse = Map.of(
        "error", "Validation failed",
        "violations", violations,
        "violationCount", result.getViolationCount()
    );

    return ResponseEntity.badRequest().body(errorResponse);
}
```

### 4. Batch Validation

```java
List<ValidatedEmailRequestDto> emailRequests = getEmailRequests();
List<ValidationResultDto> results = new ArrayList<>();

for (ValidatedEmailRequestDto emailRequest : emailRequests) {
    ValidationResultDto result = validationService.validateEmailRequest(emailRequest);
    results.add(result);
}

ValidationSummaryDto summary = validationService.getValidationSummary(results);

if (summary.isAllValid()) {
    // All validations passed
    processAllEmails(emailRequests);
} else {
    // Some validations failed
    List<ValidationViolationDto> allViolations = validationService.getAllViolations(results);
    handleValidationErrors(allViolations);
}
```

## Custom Validation Annotations

You can create custom validation annotations for specific business rules:

```java
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.matches("^\\+[1-9]\\d{1,14}$");
    }
}
```

## Best Practices

1. **Use Descriptive Messages**: Provide clear, user-friendly validation messages
2. **Cross-field Validation**: Use `@AssertTrue` methods for complex validation logic
3. **Validation Groups**: Use validation groups for different scenarios (Create, Update, etc.)
4. **Performance**: Validate early in the request processing pipeline
5. **Error Handling**: Provide comprehensive error information to clients
6. **Logging**: Log validation failures for debugging and monitoring
7. **Testing**: Write unit tests for validation logic
8. **Documentation**: Document validation rules and constraints

## Integration with Existing Services

The validation framework can be integrated with existing services:

```java
@Service
public class NotificationService {

    private final ValidationService validationService;
    private final EmailService emailService;

    public NotificationRequestDto sendEmail(ValidatedEmailRequestDto emailDto) {
        // Validate first
        ValidationResultDto validationResult = validationService.validateEmailRequest(emailDto);

        if (!validationResult.isValid()) {
            throw new ValidationException("Email validation failed", validationResult.getViolations());
        }

        // Proceed with sending email
        return emailService.sendEmail(
            emailDto.getSenderName(),
            emailDto.getRecipient(),
            emailDto.getSubject(),
            emailDto.getContent(),
            emailDto.getVariables()
        );
    }
}
```

## Error Handling

Create custom exceptions for validation errors:

```java
public class ValidationException extends RuntimeException {
    private final List<ValidationViolationDto> violations;

    public ValidationException(String message, List<ValidationViolationDto> violations) {
        super(message);
        this.violations = violations;
    }

    public List<ValidationViolationDto> getViolations() {
        return violations;
    }
}
```

## Monitoring and Logging

The validation framework provides comprehensive logging:

```java
@Slf4j
public class ValidationService {

    public ValidationResultDto validateNotificationRule(ValidatedNotificationRuleDto ruleDto) {
        log.debug("Validating notification rule: {}", ruleDto.getName());

        ValidationResultDto result = // validation logic

        if (!result.isValid()) {
            log.warn("Validation failed for rule {}: {} violations",
                ruleDto.getName(), result.getViolationCount());
        }

        return result;
    }
}
```

This validation framework provides a robust, type-safe way to validate DTOs with comprehensive error reporting and integration capabilities.
