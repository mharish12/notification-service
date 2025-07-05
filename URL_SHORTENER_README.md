# URL Shortener Service

A comprehensive URL shortening service integrated into the notification service project. This feature allows users to create short, memorable URLs that redirect to longer original URLs, with advanced features like analytics, expiration dates, and password protection.

## Features

### Core Features

- **URL Shortening**: Convert long URLs to short, memorable codes
- **Auto-Generated Short Codes**: Automatically generate unique short codes when not provided
- **Custom Aliases**: Create custom short codes for better branding
- **Expiration Dates**: Set automatic expiration for URLs
- **Click Tracking**: Track number of clicks and access patterns
- **Password Protection**: Secure URLs with passwords
- **Analytics**: View click statistics and popular URLs

### Advanced Features

- **IP Address Tracking**: Track visitor IP addresses
- **User Agent Tracking**: Monitor browser and device information
- **Bulk Operations**: Manage multiple URLs efficiently
- **Automatic Cleanup**: Remove expired URLs automatically
- **RESTful API**: Complete REST API for integration

## API Endpoints

### Create URL Shortener

```http
POST /api/v1/url-shortener
Content-Type: application/json

{
  "originalUrl": "https://example.com/very/long/url/that/needs/shortening",
  "shortCode": "optional-custom-code", // Optional - auto-generated if not provided
  "customAlias": "my-custom-link",
  "title": "My Custom Link",
  "description": "A description of this link",
  "expiresAt": "2024-12-31T23:59:59",
  "password": "secret123"
}
```

### Get URL Shortener by ID

```http
GET /api/v1/url-shortener/{id}
```

### Get URL Shortener by Short Code

```http
GET /api/v1/url-shortener/code/{shortCode}
```

### Get URL Shortener by Custom Alias

```http
GET /api/v1/url-shortener/alias/{customAlias}
```

### List All URL Shorteners

```http
GET /api/v1/url-shortener?activeOnly=false
```

### Update URL Shortener

```http
PUT /api/v1/url-shortener/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "expiresAt": "2024-12-31T23:59:59"
}
```

### Delete URL Shortener

```http
DELETE /api/v1/url-shortener/{id}
```

### Deactivate URL Shortener

```http
PATCH /api/v1/url-shortener/{id}/deactivate
```

### Redirect to Original URL

```http
GET /api/v1/url-shortener/redirect/{shortCode}
```

### Analytics Endpoints

#### Get Top URLs by Click Count

```http
GET /api/v1/url-shortener/analytics/top?limit=10
```

#### Get Recent URLs

```http
GET /api/v1/url-shortener/analytics/recent?days=7
```

#### Get Analytics Statistics

```http
GET /api/v1/url-shortener/analytics/stats
```

#### Cleanup Expired URLs

```http
POST /api/v1/url-shortener/cleanup/expired
```

### Short URL Redirect

```http
GET /s/{shortCode}
```

This endpoint performs an HTTP redirect to the original URL.

## Configuration

Add the following configuration to your `application.yml`:

```yaml
url-shortener:
  base-url: http://localhost:8080
  short-url-path: /s
  default-expiration-days: 30
  max-custom-alias-length: 50
  max-short-code-length: 25
  enable-password-protection: true
  enable-tracking: true
  cleanup-interval-hours: 24

# Rate Limiter Configuration
rate-limiter:
  enabled: true
  create:
    max-requests: 10 # 10 requests per minute for creating URLs
    window-minutes: 1
    timeout-seconds: 5
  read:
    max-requests: 100 # 100 requests per minute for reading URLs
    window-minutes: 1
    timeout-seconds: 2
  redirect:
    max-requests: 500 # 500 requests per minute for redirects
    window-minutes: 1
    timeout-seconds: 1
  analytics:
    max-requests: 30 # 30 requests per minute for analytics
    window-minutes: 1
    timeout-seconds: 3
  admin:
    max-requests: 20 # 20 requests per minute for admin operations
    window-minutes: 1
    timeout-seconds: 5
```

## Database Schema

The URL shortener uses the following database table:

```sql
CREATE TABLE url_shorteners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(20) NOT NULL UNIQUE,
    custom_alias VARCHAR(50),
    title VARCHAR(255),
    description VARCHAR(500),
    expires_at TIMESTAMP,
    click_count BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    password VARCHAR(255),
    last_accessed_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    modified_by VARCHAR(100)
);
```

## Usage Examples

### Creating a Simple URL Shortener

```bash
curl -X POST http://localhost:8080/api/v1/url-shortener \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.google.com/search?q=spring+boot+tutorial"
  }'
```

**Note**: If no `shortCode` is provided, the backend will automatically generate a unique 10-character alphanumeric code using cryptographically secure random generation.

### Creating a URL with Custom Alias

```bash
curl -X POST http://localhost:8080/api/v1/url-shortener \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot",
    "title": "Spring Boot GitHub Repository",
    "description": "Official Spring Boot repository on GitHub"
  }'
```

### Creating a Password-Protected URL

```bash
curl -X POST http://localhost:8080/api/v1/url-shortener \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com/private-document",
    "customAlias": "private-doc",
    "password": "secret123",
    "expiresAt": "2024-12-31T23:59:59"
  }'
```

### Getting Analytics

```bash
# Get top 10 most clicked URLs
curl http://localhost:8080/api/v1/url-shortener/analytics/top?limit=10

# Get URLs created in the last 7 days
curl http://localhost:8080/api/v1/url-shortener/analytics/recent?days=7

# Get overall statistics
curl http://localhost:8080/api/v1/url-shortener/analytics/stats
```

## Response Examples

### Create URL Response

```json
{
  "id": 1,
  "originalUrl": "https://example.com/very/long/url",
  "shortCode": "Ab3x9Y2k",
  "customAlias": "my-custom-link",
  "title": "My Custom Link",
  "description": "A description of this link",
  "expiresAt": "2024-12-31T23:59:59",
  "clickCount": 0,
  "isActive": true,
  "password": null,
  "lastAccessedAt": null,
  "ipAddress": null,
  "userAgent": null,
  "shortUrl": "http://localhost:8080/s/Ab3x9Y2k",
  "isExpired": false,
  "daysUntilExpiration": 30,
  "createdAt": "2024-01-01T10:00:00",
  "createdBy": "user123",
  "modifiedAt": "2024-01-01T10:00:00",
  "modifiedBy": "user123"
}
```

### Analytics Stats Response

```json
{
  "activeUrlCount": 150,
  "totalClicks": 1250,
  "timestamp": "2024-01-01T10:00:00"
}
```

## Error Handling

The service returns appropriate HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: URL shortener created successfully
- `400 Bad Request`: Invalid input data
- `404 Not Found`: URL shortener not found
- `410 Gone`: URL has expired
- `409 Conflict`: Short code or custom alias already exists
- `429 Too Many Requests`: Rate limit exceeded

## Rate Limiting

The URL shortener service implements comprehensive rate limiting to prevent abuse and ensure fair usage:

### Rate Limits by Operation Type

- **Create Operations**: 10 requests per minute
- **Read Operations**: 100 requests per minute
- **Redirect Operations**: 500 requests per minute
- **Analytics Operations**: 30 requests per minute
- **Admin Operations**: 20 requests per minute

### Rate Limiting Features

- **Resilience4j Integration**: Uses Resilience4j's robust rate limiting implementation
- **Configurable Limits**: All limits can be configured via application properties
- **Timeout Handling**: Requests wait for available slots up to a configurable timeout
- **Automatic Cleanup**: Resilience4j handles cleanup automatically
- **Production Ready**: Battle-tested rate limiting library used in production systems

### Rate Limit Response

When a rate limit is exceeded, the service returns:

```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Please try again later.",
  "retryAfter": 60
}
```

### Disabling Rate Limiting

To disable rate limiting for development, set:

```yaml
rate-limiter:
  enabled: false
```

### Resilience4j Rate Limiting

The URL shortener service uses Resilience4j for robust, production-ready rate limiting:

#### Benefits of Resilience4j Rate Limiting

- **Battle-Tested**: Used in production systems worldwide
- **Configurable**: Easy to configure different limits for different operations
- **Timeout Support**: Built-in timeout handling with configurable wait times
- **Automatic Cleanup**: Handles cleanup automatically
- **Error Handling**: Graceful error handling with fail-open behavior
- **Monitoring**: Built-in metrics and monitoring support

#### Configuration

Rate limiting is configured through application properties and automatically applied to all endpoints:

```yaml
rate-limiter:
  enabled: true
  create:
    max-requests: 10
    window-minutes: 1
    timeout-seconds: 5
  # ... other configurations
```

## Security Considerations

1. **Input Validation**: All inputs are validated using Bean Validation annotations
2. **SQL Injection Protection**: Uses JPA repositories with parameterized queries
3. **XSS Protection**: Input sanitization and output encoding
4. **Rate Limiting**: Implemented with configurable limits for different operations
5. **HTTPS**: Use HTTPS in production for secure URL generation

## Performance Considerations

1. **Database Indexes**: Optimized indexes on frequently queried columns
2. **Caching**: Consider implementing Redis caching for frequently accessed URLs
3. **Connection Pooling**: Configure appropriate database connection pool settings
4. **Monitoring**: Monitor database performance and query execution times

## Integration with Existing Services

The URL shortener service integrates seamlessly with the existing notification service:

- Uses the same audit framework (`BaseAuditableEntity`)
- Follows the same coding patterns and conventions
- Uses the same validation framework
- Integrates with the existing database migration system

## Future Enhancements

1. **QR Code Generation**: Generate QR codes for short URLs
2. **Bulk URL Import**: Import multiple URLs from CSV/Excel files
3. **Advanced Analytics**: Geographic tracking, referrer tracking
4. **API Rate Limiting**: Implement rate limiting for API endpoints
5. **Web Interface**: Add a web-based management interface
6. **Webhook Support**: Send notifications when URLs are accessed
7. **Custom Domains**: Support for custom domains for short URLs
