#!/bin/bash

echo "🚀 Notification Service Environment Setup"
echo "=========================================="

# Check if .env file exists
if [ -f ".env" ]; then
    echo "⚠️  .env file already exists. Backing up to .env.backup"
    cp .env .env.backup
fi

# Prompt for Gmail credentials
echo ""
echo "📧 Gmail Configuration"
echo "----------------------"
read -p "Enter your Gmail address: " gmail_username
read -s -p "Enter your Gmail App Password (16 characters): " gmail_password
echo ""

# Prompt for database configuration
echo ""
echo "🗄️  Database Configuration"
echo "-------------------------"
read -p "Database username [postgres]: " db_username
db_username=${db_username:-postgres}
read -s -p "Database password [password]: " db_password
echo ""
db_password=${db_password:-password}
read -p "Database URL [jdbc:postgresql://localhost:5432/notification_db]: " db_url
db_url=${db_url:-jdbc:postgresql://localhost:5432/notification_db}

# Create .env file
cat > .env << EOF
# Gmail Configuration
GMAIL_USERNAME=$gmail_username
GMAIL_PASSWORD=$gmail_password

# Database Configuration
DB_USERNAME=$db_username
DB_PASSWORD=$db_password
DB_URL=$db_url

# Optional: Outlook Configuration
# OUTLOOK_USERNAME=your-outlook@outlook.com
# OUTLOOK_PASSWORD=your-outlook-password

# Optional: Twilio Configuration
# TWILIO_ACCOUNT_SID=your-twilio-account-sid
# TWILIO_AUTH_TOKEN=your-twilio-auth-token
# TWILIO_FROM_NUMBER=your-twilio-whatsapp-number
EOF

echo ""
echo "✅ Environment configuration saved to .env file"
echo ""
echo "📋 Next steps:"
echo "1. Start PostgreSQL database: docker-compose up -d"
echo "2. Run the application: ./gradlew bootRun --args='--spring.profiles.active=dev'"
echo "3. Test the health endpoint: curl http://localhost:8080/health"
echo ""
echo "🔒 Security Note: Keep your .env file secure and never commit it to version control" 