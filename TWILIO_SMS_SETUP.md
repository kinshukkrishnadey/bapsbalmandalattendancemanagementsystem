# Twilio SMS OTP Integration Setup Guide

## Overview
The application uses **Twilio** for sending OTP via SMS to Indian phone numbers.

## Features
- ✅ Validates Indian phone numbers (10 digits, starting with 6, 7, 8, or 9)
- ✅ Sends OTP via SMS using Twilio
- ✅ Easy setup and configuration
- ✅ Secure - OTP not returned in API response (configurable for dev)
- ✅ Error handling and logging
- ✅ Supports both Indian and international Twilio numbers

## Twilio Setup

### Step 1: Sign Up
1. Go to https://www.twilio.com/
2. Sign up for a free account
3. Verify your email and phone number

### Step 2: Get Credentials
1. Login to Twilio Console: https://www.twilio.com/console
2. Find your **Account SID** (starts with `AC...`)
3. Find your **Auth Token** (click to reveal)
4. Copy both values

### Step 3: Get a Phone Number
1. In Twilio Console, go to **Phone Numbers** > **Manage** > **Buy a number**
2. Choose a number:
   - **Option A**: Buy an Indian number (+91) - requires verification and may have restrictions
   - **Option B**: Use a US/International number (+1) - works for Indian numbers, easier setup
3. Note your phone number in E.164 format (e.g., `+15551234567` or `+919876543210`)

### Step 4: Verify Phone Numbers (Free Tier)
- On free tier, you can only send SMS to verified phone numbers
- Go to **Phone Numbers** > **Verified Caller IDs**
- Add phone numbers you want to test with
- For production, upgrade to paid account

## Configuration

### Option 1: Environment Variables (Recommended for Production)
```bash
export TWILIO_ACCOUNT_SID="your-account-sid"
export TWILIO_AUTH_TOKEN="your-auth-token"
export TWILIO_FROM_NUMBER="+15551234567"  # or +919876543210
```

### Option 2: application.properties
```properties
# Enable SMS service
sms.provider.enabled=true

# Twilio Account SID
sms.provider.account-sid=your-account-sid

# Twilio Auth Token
sms.provider.auth-token=your-auth-token

# Twilio From Number (E.164 format)
sms.provider.from-number=+15551234567
```

### Option 3: application-local.properties (for local dev)
Create `application-local.properties` in project root:
```properties
sms.provider.enabled=true
sms.provider.account-sid=your-dev-account-sid
sms.provider.auth-token=your-dev-auth-token
sms.provider.from-number=+15551234567
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
2. Configure Twilio credentials
3. Set `app.otp.return-in-response=false`
4. OTP will be sent via SMS only
5. **Note**: On free tier, verify recipient phone numbers first

## Twilio Pricing

### Free Tier
- $15.50 free credit
- Can only send to verified numbers
- Good for development and testing

### Paid Tier
- Pay-as-you-go pricing
- India: ~₹0.50-1.00 per SMS
- US numbers: ~$0.0075 per SMS
- Check current pricing: https://www.twilio.com/pricing

## Error Handling

The system handles various error scenarios:
- Invalid phone number format
- SMS service not configured
- Twilio API errors
- Network errors
- Unverified phone numbers (free tier)

Errors are logged and appropriate HTTP status codes are returned.

## Troubleshooting

### OTP Not Received
1. **Free Tier**: Verify phone number in Twilio Console
2. Check Twilio Console > Logs > Debugger for message status
3. Verify phone number format
4. Check application logs for errors
5. Ensure `sms.provider.enabled=true`

### SMS Service Not Working
1. Verify `sms.provider.enabled=true`
2. Check Account SID and Auth Token are correct
3. Verify From Number is in E.164 format (starts with +)
4. Check Twilio account balance (if paid tier)
5. Review application logs

### Invalid Phone Number Error
- Ensure phone number is 10 digits
- Must start with 6, 7, 8, or 9
- Remove country code if included (system handles it)

### Twilio API Errors
Common errors:
- **21211**: Invalid 'To' phone number
- **21610**: Unsubscribed recipient (free tier)
- **21408**: Permission to send SMS denied (verify number on free tier)
- **20003**: Authentication failed (check credentials)

Check Twilio Console > Logs > Debugger for detailed error messages.

## Security Notes

- **Never return OTP in production**: Set `app.otp.return-in-response=false`
- **Use environment variables** for credentials in production
- **Rotate Auth Token** regularly
- **Monitor usage** in Twilio Console
- **Set rate limits** to prevent abuse
- **Use verified numbers** on free tier

## Production Checklist

- [ ] Upgrade Twilio account to paid tier (if needed)
- [ ] Set `sms.provider.enabled=true`
- [ ] Set `app.otp.return-in-response=false`
- [ ] Configure credentials via environment variables
- [ ] Test with real phone numbers
- [ ] Monitor Twilio Console for delivery status
- [ ] Set up alerts for failed messages
- [ ] Review Twilio pricing and set budget alerts

## Support

- Twilio Support: https://support.twilio.com/
- Twilio Console: https://www.twilio.com/console
- Twilio Docs: https://www.twilio.com/docs
- Application Logs: Check Spring Boot logs for detailed error messages

## Alternative: Using Indian Twilio Number

If you want to use an Indian Twilio number (+91):
1. Contact Twilio support for Indian number availability
2. Complete additional verification (may be required)
3. Use `+919876543210` format for `sms.provider.from-number`
4. Note: Indian numbers may have different pricing

## Migration from MSG91

If you were using MSG91 and want to switch to Twilio:
1. The `Msg91SmsService` is still available but disabled
2. `TwilioSmsService` is the active service by default
3. Just configure Twilio credentials - no code changes needed
4. The `SmsService` interface ensures compatibility
