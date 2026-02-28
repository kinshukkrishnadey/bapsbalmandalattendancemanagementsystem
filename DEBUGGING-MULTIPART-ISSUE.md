# Root Cause Analysis: Multipart Content-Type Issue

## Problem Summary
Frontend sends `multipart/form-data` with correct boundary, but Spring Boot receives `application/octet-stream`, causing `415 Unsupported Media Type` error.

## Code Flow Analysis

### 1. Controller Layer (`KidController.java`)
```java
@PostMapping("/register")
public ResponseEntity<Kid> addKid(@RequestPart("kid") Kid kid,
                                  @RequestPart(value = "photo", required = false) MultipartFile photo)
```
- ✅ Uses `@RequestPart` correctly for multipart
- ✅ Expects `kid` as JSON string and `photo` as file

### 2. Service Layer (`KidServiceImpl.java`)
```java
public Kid addKid(Kid kid, MultipartFile photo) {
    if (photo != null && !photo.isEmpty()) {
        String photoUrl = uploadToGCS(photo);
        // ⚠️ ISSUE: photoUrl is not assigned to kid!
    }
    return kidRepository.save(kid);
}
```
- ⚠️ **BUG FOUND**: `photoUrl` is generated but never set to `kid.setPhotoUrl(photoUrl)`
- ✅ Otherwise logic looks correct

### 3. Model Layer (`Kid.java`)
- ✅ Entity is properly configured
- ✅ All fields are present

## Root Cause Hypothesis

The issue is likely happening **BETWEEN** the frontend and Spring Boot:

1. **Frontend sends correctly**: `multipart/form-data; boundary=----WebKitFormBoundary...`
2. **Something in the middle changes it**: Could be:
   - **Vite dev proxy** (if using Vite)
   - **Browser extension**
   - **Network proxy**
   - **Spring Security filter** (unlikely but possible)
   - **Tomcat connector configuration**

## Debugging Steps

### Step 1: Verify Frontend is Sending Correct Content-Type

**In Browser DevTools (Chrome/Edge):**
1. Open DevTools (F12)
2. Go to **Network** tab
3. Make the request from your frontend
4. Click on the request to `/api/kid/register`
5. Check **Request Headers** section
6. Look for `Content-Type` header

**Expected:**
```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
```

**If you see:**
```
Content-Type: application/octet-stream
```
→ **Frontend is the problem** - check your FormData code

**If you see:**
```
Content-Type: multipart/form-data; boundary=...
```
→ **Frontend is correct** - problem is in transit or backend

### Step 2: Check Request Payload in DevTools

1. In Network tab, click the request
2. Go to **Payload** or **Request** tab
3. Verify you see:
   - `kid`: JSON string
   - `photo`: File object (if provided)

### Step 3: Add Backend Logging

Add this to `KidController.java` to see what Spring receives:

```java
@PostMapping("/register")
public ResponseEntity<Kid> addKid(
        @RequestPart("kid") Kid kid,
        @RequestPart(value = "photo", required = false) MultipartFile photo,
        HttpServletRequest request) {  // Add this parameter
    
    // Add logging
    System.out.println("=== MULTIPART DEBUG ===");
    System.out.println("Content-Type: " + request.getContentType());
    System.out.println("Content-Length: " + request.getContentLength());
    System.out.println("Kid received: " + kid);
    System.out.println("Photo received: " + (photo != null ? photo.getOriginalFilename() : "null"));
    System.out.println("======================");
    
    return new ResponseEntity<Kid>(kidService.addKid(kid, photo), HttpStatus.CREATED);
}
```

### Step 4: Check for Proxy Configuration

**If using Vite dev server**, check `vite.config.js`:
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:3900',
        changeOrigin: true,
        // ⚠️ Check if there's any header manipulation here
      }
    }
  }
}
```

**Common issue**: Some proxies strip or modify `Content-Type` headers.

### Step 5: Test with curl (Bypass Frontend)

Use the curl command below to test if backend works correctly when Content-Type is correct.

## curl Command to Test

```bash
# Step 1: Get OTP
curl -X POST "http://localhost:3900/auth/request-otp?phone=9876543210"

# Step 2: Login (replace OTP from step 1)
curl -X POST "http://localhost:3900/auth/login?phone=9876543210&otp=YOUR_OTP_HERE"

# Step 3: Register Kid (replace TOKEN from step 2)
curl -X POST "http://localhost:3900/api/kid/register" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "kid={\"firstName\":\"Test\",\"lastName\":\"Kid\",\"gender\":\"BAL\",\"dateOfBirth\":\"2016-03-15\",\"age\":8,\"motherName\":\"Test Mother\",\"fatherName\":\"Test Father\",\"phoneNumber\":\"9876543211\",\"emailAddress\":\"test@example.com\",\"houseNumber\":\"123\",\"area\":\"Test Area\",\"state\":\"Delhi\",\"pincode\":110001,\"sabhaKshetra\":{\"kshetraId\":1},\"status\":{\"statusId\":1},\"roles\":[{\"roleId\":1}]}" \
  -F "photo=@/path/to/your/test-image.jpg"

# Note: Replace /path/to/your/test-image.jpg with actual image path
# On Windows PowerShell, use: -F "photo=@C:\Users\YourName\Pictures\test.jpg"
```

**If curl works but frontend doesn't:**
→ Problem is in frontend or proxy

**If curl also fails:**
→ Problem is in backend configuration

## Additional Checks

### Check Spring Boot Application Properties

Verify `application.properties` has:
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Check for Custom Filters

Look for any filters that might modify headers:
- Security filters
- CORS filters
- Custom request wrappers

### Check Tomcat Configuration

If using embedded Tomcat, check if there's any custom connector configuration that might affect Content-Type handling.

## Quick Fix to Test

If you want to test if the issue is Content-Type related, temporarily modify `KidController` to accept `application/octet-stream`:

```java
@PostMapping(value = "/register", consumes = {"multipart/form-data", "application/octet-stream"})
```

**Note**: This is just for testing - not a permanent solution!

## Next Steps

1. Run the curl command - does it work?
2. Check browser DevTools - what Content-Type is sent?
3. Add backend logging - what Content-Type does Spring receive?
4. Check proxy configuration - is Vite/any proxy modifying headers?

Based on the results, we can pinpoint the exact cause.
