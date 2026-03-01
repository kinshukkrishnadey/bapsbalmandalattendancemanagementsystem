# SMS OTP Integration Setup Guide

## Overview
The application now supports sending OTP via SMS to Indian phone numbers using MSG91 SMS gateway.

## Features
- ✅ Validates Indian phone numbers (10 digits, starting with 6, 7, 8, or 9)
- ✅ Sends OTP via SMS using MSG91
- ✅ Configurable SMS provider (can switch to other providers)
- ✅ Secure - OTP not returned in API response (configurable for dev)
- ✅ Error handling and logging

## MSG91 Setup

### Step 1: Sign Up
1. Go to https://msg91.com/
2. Sign up for an account
3. Complete KYC verification (required for India)

### Step 2: Get API Key
1. Login to MSG91 dashboard
2. Go to **API** section
3. Copy your **Auth Key** (API Key)

### Step 3: Get Sender ID
1. Go to **Sender ID** section in dashboard
2. Request a Sender ID (6 characters, alphanumeric)
3. Wait for approval (usually takes 1-2 business days)
4. Once approved, note your Sender ID

### Step 4: DLT Registration (Required for India)
1. Register your entity on DLT portal: https://www.dltconnect.in/
2. Register your message template
3. Get your **DLT Template ID** (TE_ID)
4. Note: Without DLT registration, SMS may be blocked in India

## Configuration

### Option 1: Environment Variables (Recommended for Production)
```bash
export SMS_API_KEY="your-msg91-api-key"
export SMS_SENDER_ID="YOURID"
export SMS_TEMPLATE_ID="your-dlt-template-id"  # Optional
```

### Option 2: application.properties
```properties
# Enable SMS service
sms.provider.enabled=true

# MSG91 API Key
sms.provider.api-key=your-msg91-api-key

# MSG91 Sender ID (6 characters)
sms.provider.sender-id=YOURID

# DLT Template ID (optional)
sms.provider.template-id=your-dlt-template-id

# MSG91 API URL (default)
sms.provider.url=https://control.msg91.com/api/sendotp.php
```

### Option 3: application-local.properties (for local dev)
Create `application-local.properties` in project root:
```properties
sms.provider.enabled=true
sms.provider.api-key=your-dev-api-key
sms.provider.sender-id=YOURID
```

## Phone Number Format

The system accepts Indian phone numbers in various formats:
- `9876543210` (10 digits)
- `+919876543210` (with country code)
- `91 9876543210` (with space)

All formats are normalized to `+919876543210` before sending SMS.

## API Usage

### Request OTP
```bash
POST /auth/request-otp?phone=9876543210
```

**Response (Production):**
```json
{
  "message": "OTP sent successfully to your registered mobile number"
}
```

**Response (Dev Mode - when `app.otp.return-in-response=true`):**
```json
{
  "message": "OTP sent successfully",
  "otp": "123456"
}
```

### Login with OTP
```bash
POST /auth/login?phone=9876543210&otp=123456
```

## Testing

### Test Without SMS (Development)
1. Set `sms.provider.enabled=false`
2. Set `app.otp.return-in-response=true`
3. OTP will be returned in API response for testing

### Test With SMS (Production-like)
1. Set `sms.provider.enabled=true`
2. Configure MSG91 credentials
3. Set `app.otp.return-in-response=false`
4. OTP will be sent via SMS only

## Error Handling

The system handles various error scenarios:
- Invalid phone number format
- SMS service not configured
- SMS sending failure
- Network errors

Errors are logged and appropriate HTTP status codes are returned.

## Alternative SMS Providers

To use a different SMS provider:
1. Implement the `SmsService` interface
2. Create a new service class (e.g., `TwilioSmsService`, `TextLocalSmsService`)
3. Update Spring configuration to use the new service

## Troubleshooting

### OTP Not Received
1. Check MSG91 dashboard for delivery status
2. Verify phone number format
3. Check DLT registration status
4. Verify Sender ID is approved
5. Check application logs for errors

### SMS Service Not Working
1. Verify `sms.provider.enabled=true`
2. Check API key is correct
3. Verify Sender ID is approved
4. Check network connectivity
5. Review application logs

### Invalid Phone Number Error
- Ensure phone number is 10 digits
- Must start with 6, 7, 8, or 9
- Remove country code if included (system handles it)

## Security Notes

- **Never return OTP in production**: Set `app.otp.return-in-response=false`
- **Use environment variables** for API keys in production
- **Rotate API keys** regularly
- **Monitor SMS usage** in MSG91 dashboard
- **Set rate limits** to prevent abuse

## Cost Considerations

MSG91 pricing (approximate):
- ~₹0.15-0.30 per SMS in India
- Bulk discounts available
- Check current pricing on MSG91 website

## Support

- MSG91 Support: https://msg91.com/support
- DLT Portal: https://www.dltconnect.in/
- Application Logs: Check Spring Boot logs for detailed error messages
