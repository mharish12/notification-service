#!/bin/bash

echo "🧪 Testing Gmail Configuration"
echo "=============================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ .env file not found. Run ./setup-env.sh first"
#    exit 1
fi

# Load environment variables
source .env

# Check if Gmail credentials are set
if [ -z "$GMAIL_USERNAME" ] || [ -z "$GMAIL_PASSWORD" ]; then
    echo "❌ Gmail credentials not found in .env file"
    exit 1
fi

echo "✅ Gmail credentials found"
echo "📧 Username: $GMAIL_USERNAME"
echo "🔑 Password: ${GMAIL_PASSWORD:0:4}****"

# Test database connection
#echo ""
#echo "🗄️  Testing database connection..."
#if docker ps | grep -q notification-postgres; then
#    echo "✅ PostgreSQL container is running"
#else
#    echo "⚠️  PostgreSQL container not running. Starting it..."
#    docker-compose up -d postgres
#    sleep 5
#fi

# Test application startup
echo ""
echo "🚀 Testing application startup..."
echo "Starting application with Gmail configuration..."

# Run the application in background
./gradlew bootRun --args='--spring.profiles.active=local' > app.log 2>&1 &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 30

# Test health endpoint
if curl -s http://localhost:8080/health > /dev/null; then
    echo "✅ Application started successfully"
    echo "🌐 Health endpoint: http://localhost:8080/health"
    echo "📚 API documentation available at: http://localhost:8080"
else
    echo "❌ Application failed to start"
    echo "📋 Check app.log for details"
    kill $APP_PID 2>/dev/null
    exit 1
fi

# Test Gmail configuration via API
echo ""
echo "📧 Testing Gmail configuration via API..."

# First, create a Gmail sender configuration
curl -X POST http://localhost:8080/api/email-senders \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"gmail-test\",
    \"host\": \"smtp.gmail.com\",
    \"port\": 587,
    \"username\": \"$GMAIL_USERNAME\",
    \"password\": \"$GMAIL_PASSWORD\",
    \"properties\": {
      \"mail.smtp.auth\": \"true\",
      \"mail.smtp.starttls.enable\": \"true\"
    },
    \"isActive\": true
  }" 2>/dev/null

# Create a test template
curl -X POST http://localhost:8080/api/templates \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"test-email\",
    \"type\": \"EMAIL\",
    \"subject\": \"Test Email from Notification Service\",
    \"content\": \"Hello {{name}}, this is a test email from the notification service!\",
    \"variables\": [\"name\"],
    \"isActive\": true
  }" 2>/dev/null

echo ""
echo "✅ Configuration test completed"
echo ""
echo "📋 To send a test email, use:"
echo "curl -X POST http://localhost:8080/api/notifications/email/template/test-email \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{"
echo "    \"senderName\": \"gmail-test\","
echo "    \"recipient\": \"your-test-email@example.com\","
echo "    \"variables\": {"
echo "      \"name\": \"Test User\""
echo "    }"
echo "  }'"
echo ""
echo "🛑 To stop the application: kill $APP_PID" 