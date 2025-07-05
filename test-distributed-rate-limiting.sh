#!/bin/bash

# Test script for Resilience4j rate limiting
# This script helps test rate limiting functionality

echo "🚀 Testing Resilience4j Rate Limiting"
echo "====================================="

# Configuration
BASE_URL="http://localhost:8080"
API_ENDPOINT="/api/v1/url-shortener"
RATE_LIMIT=10  # Should match your configuration
WINDOW_MINUTES=1

echo "📊 Rate Limit Configuration:"
echo "   - Max Requests: $RATE_LIMIT per $WINDOW_MINUTES minute(s)"
echo "   - Endpoint: $BASE_URL$API_ENDPOINT"
echo ""

# Test 1: Normal requests within limit
echo "🧪 Test 1: Normal requests within limit"
echo "Making $RATE_LIMIT requests..."

for i in $(seq 1 $RATE_LIMIT); do
    response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$BASE_URL$API_ENDPOINT" \
        -H "Content-Type: application/json" \
        -d "{\"originalUrl\": \"https://example.com/test$i\"}")
    
    if [ "$response" = "201" ]; then
        echo "   ✅ Request $i: SUCCESS (HTTP $response)"
    else
        echo "   ❌ Request $i: FAILED (HTTP $response)"
    fi
done

echo ""

# Test 2: Exceeding rate limit
echo "🧪 Test 2: Exceeding rate limit"
echo "Making 3 additional requests (should be rate limited)..."

for i in $(seq 1 3); do
    response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$BASE_URL$API_ENDPOINT" \
        -H "Content-Type: application/json" \
        -d "{\"originalUrl\": \"https://example.com/overflow$i\"}")
    
    if [ "$response" = "429" ]; then
        echo "   ✅ Request $i: RATE LIMITED (HTTP $response) - Expected"
    elif [ "$response" = "201" ]; then
        echo "   ⚠️  Request $i: SUCCESS (HTTP $response) - Rate limit not working"
    else
        echo "   ❌ Request $i: UNEXPECTED (HTTP $response)"
    fi
done

echo ""

# Test 3: Wait for window to reset
echo "🧪 Test 3: Waiting for rate limit window to reset..."
echo "Waiting $WINDOW_MINUTES minute(s) for rate limit to reset..."

sleep $((WINDOW_MINUTES * 60))

echo "Making a new request after window reset..."

response=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL$API_ENDPOINT" \
    -H "Content-Type: application/json" \
    -d "{\"originalUrl\": \"https://example.com/after-reset\"}")

if [ "$response" = "201" ]; then
    echo "   ✅ Request after reset: SUCCESS (HTTP $response) - Rate limit reset working"
else
    echo "   ❌ Request after reset: FAILED (HTTP $response) - Rate limit not reset"
fi

echo ""
echo "🎉 Rate limiting test completed!"
echo ""
echo "💡 Tips:"
echo "   - Check your application logs for rate limiting messages"
echo "   - Resilience4j automatically handles rate limiting state"
echo "   - Adjust the RATE_LIMIT variable in this script to match your config" 