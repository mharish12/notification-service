# Notification Rule Engine

The Notification Rule Engine allows you to create sophisticated rules for controlling when and how notifications are sent to users. Rules can be based on time, frequency, content, or a combination of these factors.

## Features

- **Time-based Rules**: Control notifications based on time of day, day of week, and timezone
- **Frequency-based Rules**: Limit notifications per day or set minimum intervals between notifications
- **Content-based Rules**: Filter notifications based on content length, keywords, and variables
- **Composite Rules**: Combine multiple rule types for complex scenarios
- **Priority System**: Rules are evaluated in priority order
- **User-specific Rules**: Each user can have their own set of rules
- **Template Integration**: Rules can use notification templates
- **Real-time Evaluation**: Rules are evaluated before each notification is sent

## Rule Types

### 1. Time-based Rules

Control when notifications can be sent based on time constraints.

**Supported Conditions:**

- Days of the week (Monday, Tuesday, etc.)
- Time ranges (start time to end time)
- Timezone support

**Example:**

```json
{
  "name": "Business Hours Only",
  "userId": "user123",
  "ruleType": "TIME_BASED",
  "notificationType": "EMAIL",
  "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "timezone": "America/New_York",
  "actionType": "SEND_NOTIFICATION"
}
```

### 2. Frequency-based Rules

Control how often notifications can be sent.

**Supported Conditions:**

- Maximum notifications per day
- Minimum interval between notifications (in minutes)

**Example:**

```json
{
  "name": "Daily Limit",
  "userId": "user123",
  "ruleType": "FREQUENCY_BASED",
  "notificationType": "WHATSAPP",
  "maxNotificationsPerDay": 5,
  "minIntervalMinutes": 30,
  "actionType": "SEND_NOTIFICATION"
}
```

### 3. Content-based Rules

Filter notifications based on content and variables.

**Supported Conditions:**

- Maximum content length
- Blocked keywords
- Required keywords
- Variable conditions (equals, not equals, contains, min/max length)

**Example:**

```json
{
  "name": "Content Filter",
  "userId": "user123",
  "ruleType": "CONTENT_BASED",
  "notificationType": "EMAIL",
  "conditions": {
    "maxContentLength": 1000,
    "blockedKeywords": ["spam", "urgent", "limited time"],
    "requiredKeywords": ["important"],
    "variableConditions": {
      "category": {
        "equals": "alert"
      },
      "priority": {
        "notEquals": "low"
      }
    }
  },
  "actionType": "SEND_NOTIFICATION"
}
```

### 4. Composite Rules

Combine multiple rule types for complex scenarios.

**Example:**

```json
{
  "name": "Complex Rule",
  "userId": "user123",
  "ruleType": "COMPOSITE",
  "notificationType": "EMAIL",
  "conditions": {
    "requireAll": true,
    "timeBased": true,
    "frequencyBased": true,
    "contentBased": true
  },
  "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "maxNotificationsPerDay": 3,
  "actionType": "SEND_NOTIFICATION"
}
```

## API Endpoints

### Rule Management

#### Create a Rule

```http
POST /api/rules
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

#### Get Rules for User

```http
GET /api/rules/user/{userId}
```

#### Get Rule by ID

```http
GET /api/rules/{id}
```

#### Update Rule

```http
PUT /api/rules/{id}
Content-Type: application/json

{
  "name": "Updated Rule",
  "priority": 2
}
```

#### Delete Rule

```http
DELETE /api/rules/{id}
```

#### Activate/Deactivate Rule

```http
POST /api/rules/{id}/activate
POST /api/rules/{id}/deactivate
```

#### Update Rule Priority

```http
PATCH /api/rules/{id}/priority
Content-Type: application/json

{
  "priority": 5
}
```

### Rule Evaluation

#### Evaluate Rules for User

```http
POST /api/rules/evaluate/{userId}
Content-Type: application/json

{
  "content": "This is a test notification",
  "variables": {
    "userId": "user123",
    "category": "alert",
    "priority": "high"
  }
}
```

#### Test Rule Evaluation

```http
POST /api/rules/test-evaluation
Content-Type: application/json

{
  "userId": "user123",
  "content": "Test content",
  "variables": {
    "category": "alert"
  }
}
```

## Integration with Notification Services

The rule engine is automatically integrated with all notification services:

### Email Notifications

When sending emails, include the `userId` in the variables:

```json
{
  "senderName": "system",
  "recipient": "user@example.com",
  "subject": "Important Update",
  "content": "Your account has been updated",
  "variables": {
    "userId": "user123",
    "category": "account"
  }
}
```

### WhatsApp Notifications

```json
{
  "toNumber": "+1234567890",
  "content": "Your order is ready",
  "variables": {
    "userId": "user123",
    "orderId": "ORD-123"
  }
}
```

### Template-based Notifications

```json
{
  "senderName": "system",
  "recipient": "user@example.com",
  "variables": {
    "userId": "user123",
    "name": "John Doe",
    "orderNumber": "ORD-123"
  }
}
```

## Rule Actions

Rules can perform different actions:

- **SEND_NOTIFICATION**: Allow the notification to be sent (default)
- **BLOCK**: Prevent the notification from being sent
- **MODIFY**: Modify the notification content or variables before sending

## Priority System

Rules are evaluated in priority order (highest priority first). When multiple rules apply:

1. If any rule has action type "BLOCK", the notification is blocked
2. Otherwise, all applicable rules are applied
3. User statistics are updated for frequency-based rules

## User Statistics

The rule engine maintains user statistics for frequency-based rules:

- Daily notification count (resets at midnight)
- Last notification timestamp
- Used for enforcing daily limits and minimum intervals

## Best Practices

1. **Use Descriptive Names**: Give rules meaningful names for easy management
2. **Set Appropriate Priorities**: Higher priority rules are evaluated first
3. **Test Rules**: Use the test evaluation endpoint to verify rule behavior
4. **Monitor Performance**: Rules are evaluated for every notification, so keep them efficient
5. **Use Templates**: Leverage notification templates for consistent messaging
6. **Timezone Awareness**: Always specify timezone for time-based rules

## Example Use Cases

### 1. Business Hours Only

```json
{
  "name": "Business Hours",
  "userId": "user123",
  "ruleType": "TIME_BASED",
  "notificationType": "EMAIL",
  "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "timezone": "America/New_York"
}
```

### 2. Rate Limiting

```json
{
  "name": "Rate Limit",
  "userId": "user123",
  "ruleType": "FREQUENCY_BASED",
  "notificationType": "WHATSAPP",
  "maxNotificationsPerDay": 10,
  "minIntervalMinutes": 15
}
```

### 3. Content Filtering

```json
{
  "name": "No Spam",
  "userId": "user123",
  "ruleType": "CONTENT_BASED",
  "notificationType": "EMAIL",
  "conditions": {
    "blockedKeywords": ["spam", "urgent", "limited time offer"],
    "maxContentLength": 500
  }
}
```

### 4. VIP User Rules

```json
{
  "name": "VIP User",
  "userId": "vip123",
  "ruleType": "COMPOSITE",
  "notificationType": "EMAIL",
  "priority": 10,
  "maxNotificationsPerDay": 50,
  "conditions": {
    "requireAll": false
  }
}
```

## Database Schema

The rule engine uses the following database tables:

- `notification_rules`: Main rules table
- `rule_days_of_week`: Days of week for time-based rules

Key indexes are created for optimal performance:

- User ID + Active + Priority
- Rule type
- Notification type
- Template ID

## Monitoring and Logging

The rule engine provides comprehensive logging:

- Rule evaluation results
- Blocked notifications with reasons
- Applied rules for each notification
- User statistics updates

Check the application logs for detailed rule evaluation information.
