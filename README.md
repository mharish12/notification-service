# Notification Service

A comprehensive Spring Boot notification service that supports email and WhatsApp notifications with template management, multiple email sender configurations, and PostgreSQL persistence.

## Features

- **Email Notifications**: Send emails using multiple SMTP configurations (Gmail, Outlook, custom)
- **WhatsApp Notifications**: Send WhatsApp messages using Twilio
- **Template Management**: Create and manage email and WhatsApp templates with variable substitution
- **Database Persistence**: Store templates, notification requests, and responses in PostgreSQL
- **Multiple Email Senders**: Configure and use different email providers
- **RESTful API**: Complete REST API for all operations
- **Template Processing**: Variable substitution using `{{variable}}` syntax

## Technology Stack

- **Spring Boot 3.2.0**
- **Java 17**
- **Gradle**
- **PostgreSQL**
- **Flyway** (Database migrations)
- **Twilio** (WhatsApp messaging)
- **Lombok**
- **JPA/Hibernate**

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.0 or higher
- Twilio account (for WhatsApp functionality)

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE notification_db;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE notification_db TO postgres;
```

### 2. Configuration

Update the `application.yml` file with your database and email configurations:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notification_db
    username: your_username
    password: your_password

notification:
  email:
    senders:
      gmail:
        host: smtp.gmail.com
        port: 587
        username: your-gmail@gmail.com
        password: your-gmail-app-password
        properties:
          mail.smtp.auth: 'true'
          mail.smtp.starttls.enable: 'true'
      outlook:
        host: smtp-mail.outlook.com
        port: 587
        username: your-outlook@outlook.com
        password: your-outlook-password
        properties:
          mail.smtp.auth: 'true'
          mail.smtp.starttls.enable: 'true'

  whatsapp:
    twilio:
      account-sid: your-twilio-account-sid
      auth-token: your-twilio-auth-token
      from-number: your-twilio-whatsapp-number
```

### 3. Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Documentation

### Templates

#### Get All Templates

```http
GET /api/templates
```

#### Get Templates by Type

```http
GET /api/templates/type/EMAIL
GET /api/templates/type/WHATSAPP
```

#### Get Template by ID

```http
GET /api/templates/{id}
```

#### Get Template by Name

```http
GET /api/templates/name/{name}
```

#### Create Template

```http
POST /api/templates
Content-Type: application/json

{
  "name": "welcome-email",
  "type": "EMAIL",
  "subject": "Welcome to Our Service",
  "content": "Hello {{name}}, Welcome to our service!",
  "variables": ["name"],
  "isActive": true
}
```

#### Update Template

```http
PUT /api/templates/{id}
Content-Type: application/json

{
  "name": "welcome-email",
  "type": "EMAIL",
  "subject": "Welcome to Our Service",
  "content": "Hello {{name}}, Welcome to our service!",
  "variables": ["name"],
  "isActive": true
}
```

#### Delete Template

```http
DELETE /api/templates/{id}
```

#### Process Template

```http
POST /api/templates/{name}/process
Content-Type: application/json

{
  "name": "John Doe",
  "resetLink": "https://example.com/reset"
}
```

### Email Senders

#### Get All Email Senders

```http
GET /api/email-senders
```

#### Get Email Sender by ID

```http
GET /api/email-senders/{id}
```

#### Get Email Sender by Name

```http
GET /api/email-senders/name/{name}
```

#### Create Email Sender

```http
POST /api/email-senders
Content-Type: application/json

{
  "name": "gmail",
  "host": "smtp.gmail.com",
  "port": 587,
  "username": "your-email@gmail.com",
  "password": "your-app-password",
  "properties": {
    "mail.smtp.auth": "true",
    "mail.smtp.starttls.enable": "true"
  },
  "isActive": true
}
```

#### Update Email Sender

```http
PUT /api/email-senders/{id}
Content-Type: application/json

{
  "name": "gmail",
  "host": "smtp.gmail.com",
  "port": 587,
  "username": "your-email@gmail.com",
  "password": "your-app-password",
  "properties": {
    "mail.smtp.auth": "true",
    "mail.smtp.starttls.enable": "true"
  },
  "isActive": true
}
```

#### Delete Email Sender

```http
DELETE /api/email-senders/{id}
```

### Notifications

#### Send Email

```http
POST /api/notifications/email
Content-Type: application/json

{
  "senderName": "gmail",
  "recipient": "recipient@example.com",
  "subject": "Test Email",
  "content": "Hello {{name}}, this is a test email.",
  "variables": {
    "name": "John Doe"
  }
}
```

#### Send Email with Template

```http
POST /api/notifications/email/template/welcome-email
Content-Type: application/json

{
  "senderName": "gmail",
  "recipient": "recipient@example.com",
  "variables": {
    "name": "John Doe"
  }
}
```

#### Send WhatsApp Message

```http
POST /api/notifications/whatsapp
Content-Type: application/json

{
  "toNumber": "+1234567890",
  "content": "Hello {{name}}, this is a test WhatsApp message.",
  "variables": {
    "name": "John Doe"
  }
}
```

#### Send WhatsApp with Template

```http
POST /api/notifications/whatsapp/template/whatsapp-welcome
Content-Type: application/json

{
  "toNumber": "+1234567890",
  "variables": {
    "name": "John Doe"
  }
}
```

## Database Schema

The service uses the following tables:

- `notification_templates`: Store email and WhatsApp templates
- `email_senders`: Store SMTP configurations for different email providers
- `notification_requests`: Track all notification requests
- `notification_responses`: Store responses from notification providers

## Template Variables

Templates support variable substitution using the `{{variable}}` syntax:

```html
Hello {{name}}, your order #{{orderId}} has been confirmed. Total amount:
${{amount}}
```

When sending a notification, provide the variables:

```json
{
  "name": "John Doe",
  "orderId": "12345",
  "amount": "99.99"
}
```

## Email Provider Setup

### Gmail

1. Enable 2-factor authentication
2. Generate an App Password
3. Use the App Password in the configuration

### Outlook

1. Use your email and password
2. Enable SMTP authentication in your account settings

### Custom SMTP

Configure your own SMTP server settings in the configuration.

## WhatsApp Setup

1. Create a Twilio account
2. Get your Account SID and Auth Token
3. Set up a WhatsApp number in Twilio
4. Configure the settings in `application.yml`

## Development

### Running Tests

```bash
./gradlew test
```

### Database Migrations

The application uses Flyway for database migrations. Migrations are automatically applied on startup.

### Logging

Logs are configured to show SQL queries and email operations. Adjust the logging level in `application.yml` as needed.

## Error Handling

The service includes comprehensive error handling:

- Template not found errors
- Email sender configuration errors
- SMTP connection errors
- Twilio API errors
- Database constraint violations

All errors are logged and appropriate HTTP status codes are returned.

## Security Considerations

- Store sensitive information (passwords, API keys) in environment variables
- Use HTTPS in production
- Implement proper authentication and authorization
- Regularly rotate API keys and passwords
- Monitor and log all notification activities

## Production Deployment

1. Set up a production PostgreSQL database
2. Configure environment variables for sensitive data
3. Set up proper logging and monitoring
4. Configure SSL/TLS for secure communication
5. Set up backup and recovery procedures
6. Monitor notification delivery rates and failures
