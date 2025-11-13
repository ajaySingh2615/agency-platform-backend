# Step 11: Comprehensive Testing Guide & Security Checklist

## 🧪 **Complete API Testing Flow**

This guide walks you through testing the entire user management system from start to finish.

---

## 📋 **Prerequisites**

1. **Start the server**:

   ```bash
   npm run dev
   ```

2. **Ensure PostgreSQL is running**

3. **Open Postman or use curl commands below**

4. **Create a test log file** to track your responses:
   ```bash
   touch test-results.md
   ```

---

## 🔄 **Test Flow 1: Phone Authentication (New User)**

### Step 1: Health Check

```bash
curl http://localhost:5000/health
```

✅ **Expected**: Status 200, "Server is running"

### Step 2: Send OTP

```bash
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'
```

✅ **Expected**:

- Status 200
- Message: "OTP sent successfully"
- Check console for OTP code

📝 **Save**: OTP code from console

### Step 3: Verify OTP (First Time)

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "PASTE_OTP_HERE",
    "deviceInfo": "Postman - Windows"
  }'
```

✅ **Expected**:

- Status 200
- Message: "Account created successfully"
- `isNewUser: true`
- `user.role: null`
- Receive `accessToken` and `refreshToken`

📝 **Save**: Both tokens in a text file

### Step 4: Get Profile (No Role)

```bash
curl -X GET http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- `role: null`

### Step 5: Select Role

```bash
curl -X PATCH http://localhost:5000/api/users/me/role \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "creator"}'
```

✅ **Expected**:

- Status 200
- Message: "Role selected successfully"
- Receive new `accessToken` with role

📝 **Save**: New access token (replaces old one)

### Step 6: Update Profile

```bash
curl -X PATCH http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_NEW_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "profilePictureUrl": "https://i.pravatar.cc/150?img=1"
  }'
```

✅ **Expected**:

- Status 200
- Profile updated with new data

### Step 7: Get Detailed Profile

```bash
curl -X GET http://localhost:5000/api/users/me/detailed \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- Full user data with `authMethods` array
- Should show phone provider

---

## 🔄 **Test Flow 2: Token Management**

### Step 8: View Active Sessions

```bash
curl -X GET http://localhost:5000/api/auth/sessions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- Array of sessions (should have 1)

### Step 9: Login from Another Device

Repeat steps 2-3 with different `deviceInfo`:

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "NEW_OTP",
    "deviceInfo": "iPhone - Safari"
  }'
```

✅ **Expected**:

- Status 200
- Message: "Login successful"
- `isNewUser: false`
- New tokens for this device

📝 **Save**: Second set of tokens

### Step 10: Check Sessions (Should Have 2)

```bash
curl -X GET http://localhost:5000/api/auth/sessions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- 2 sessions with different deviceInfo

### Step 11: Test Device Limit (Login 3rd Time)

Repeat login to trigger 2-device limit:

```bash
# Send OTP again
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'

# Verify OTP
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "THIRD_OTP",
    "deviceInfo": "iPad - Chrome"
  }'
```

✅ **Expected**:

- Status 200
- Console log: "Removed oldest session for user..."
- Still only 2 sessions (oldest removed)

### Step 12: Refresh Access Token

```bash
curl -X POST http://localhost:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

✅ **Expected**:

- Status 200
- New access token

### Step 13: Logout from One Device

```bash
curl -X POST http://localhost:5000/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

✅ **Expected**:

- Status 200
- Message: "Logged out successfully"

### Step 14: Try to Refresh with Logged Out Token (Should Fail)

```bash
curl -X POST http://localhost:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_LOGGED_OUT_REFRESH_TOKEN"}'
```

✅ **Expected**:

- Status 401
- Message: "Refresh token not found"

### Step 15: Logout from All Devices

```bash
curl -X POST http://localhost:5000/api/auth/logout-all \
  -H "Authorization: Bearer YOUR_VALID_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- All sessions cleared

---

## 🔄 **Test Flow 3: Error Handling**

### Test 16: Invalid Phone Number

```bash
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "123"}'
```

✅ **Expected**:

- Status 400
- Validation error message

### Test 17: Invalid OTP

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "000000",
    "deviceInfo": "Test"
  }'
```

✅ **Expected**:

- Status 401
- Message: "Invalid OTP"

### Test 18: Expired OTP

Wait 6 minutes after sending OTP, then try to verify.

✅ **Expected**:

- Status 401
- Message: "OTP has expired"

### Test 19: Missing Authorization Header

```bash
curl -X GET http://localhost:5000/api/users/me
```

✅ **Expected**:

- Status 401
- Message: "Access token is required"

### Test 20: Invalid Access Token

```bash
curl -X GET http://localhost:5000/api/users/me \
  -H "Authorization: Bearer invalid_token_here"
```

✅ **Expected**:

- Status 401
- Message: "Invalid token"

### Test 21: Try to Change Role Twice

```bash
curl -X PATCH http://localhost:5000/api/users/me/role \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "agency"}'
```

✅ **Expected**:

- Status 400
- Message: "Role has already been selected"

### Test 22: Rate Limiting - OTP Spam

Send OTP requests 4 times in a row:

```bash
# Request 1, 2, 3 - Success
# Request 4:
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'
```

✅ **Expected**:

- Status 429
- Message: "Too many OTP requests"

---

## 🔄 **Test Flow 4: Edge Cases**

### Test 23: Login with Same Phone (Existing User)

```bash
# Login again with existing phone number
# Should return isNewUser: false and existing user data
```

✅ **Expected**:

- User data with previously set role and profile

### Test 24: Update Profile with Invalid URL

```bash
curl -X PATCH http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"profilePictureUrl": "not-a-url"}'
```

✅ **Expected**:

- Status 400
- Validation error

### Test 25: Delete Account

```bash
curl -X DELETE http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 200
- Message: "Account deleted successfully"

### Test 26: Try to Access After Deletion

```bash
curl -X GET http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_OLD_ACCESS_TOKEN"
```

✅ **Expected**:

- Status 404
- Message: "User not found"

---

## 🛡️ **Security Checklist**

### ✅ Authentication & Authorization

- [ ] Access tokens expire in 15 minutes
- [ ] Refresh tokens expire in 30 days
- [ ] Tokens are validated on every protected route
- [ ] JWT secrets are stored in environment variables
- [ ] JWT secrets are strong (64+ characters)
- [ ] Tokens include `iss` (issuer) and `aud` (audience)
- [ ] Role-based access control works correctly
- [ ] Users without roles cannot access protected resources

### ✅ Password & Token Hashing

- [ ] OTPs are hashed with bcrypt (not stored plain text)
- [ ] Refresh tokens are hashed with bcrypt
- [ ] Salt rounds ≥ 12
- [ ] OTPs are deleted after verification
- [ ] Expired OTPs are automatically cleaned up

### ✅ Input Validation

- [ ] All inputs are validated with Zod schemas
- [ ] Phone numbers are normalized to E.164
- [ ] Email format is validated
- [ ] URLs are validated before storage
- [ ] SQL injection is prevented (Drizzle parameterized queries)

### ✅ Rate Limiting

- [ ] OTP sending is limited (3/hour)
- [ ] OTP verification is limited (5/15min)
- [ ] Token refresh is limited (10/min)
- [ ] General API is limited (100/15min)
- [ ] Rate limits return 429 status code

### ✅ Error Handling

- [ ] Database errors don't expose internal details
- [ ] Stack traces are only shown in development
- [ ] Generic error messages for security (don't reveal if user exists)
- [ ] All errors are logged for debugging
- [ ] Unhandled promise rejections are caught

### ✅ Database Security

- [ ] Connection uses environment variables
- [ ] Connection pooling is configured
- [ ] Foreign key constraints are enforced
- [ ] Cascade deletes work correctly
- [ ] Unique constraints prevent duplicate accounts
- [ ] Timestamps are stored with timezone

### ✅ CORS & Headers

- [ ] CORS is configured with allowed origins
- [ ] Helmet middleware is active
- [ ] `Content-Type` is checked on all POST/PATCH/PUT
- [ ] Credentials are allowed for cookies (if used)

### ✅ Session Management

- [ ] Device limit is enforced (max 2 devices)
- [ ] Oldest session is removed when limit reached
- [ ] Users can view active sessions
- [ ] Users can revoke specific sessions
- [ ] Users can logout from all devices
- [ ] Expired tokens are cleaned up periodically

### ✅ HTTPS & Production

- [ ] Environment is set to 'production'
- [ ] HTTPS is enforced (not for localhost)
- [ ] Refresh tokens are in httpOnly cookies (recommended)
- [ ] Sensitive data is never logged
- [ ] API keys are never exposed to client

---

## 📊 **Performance Checklist**

- [ ] Database indexes are created on frequently queried fields
- [ ] Connection pooling is used (max 10 connections)
- [ ] Expensive operations are cached (if applicable)
- [ ] Large responses are paginated
- [ ] Queries use `limit()` to prevent huge result sets

---

## 🔧 **Database Verification**

### Check Tables

```bash
npm run db:studio
```

Open `https://local.drizzle.studio` and verify:

1. **users table**:

   - Has users with various roles
   - Timestamps are correct
   - `updatedAt` changes on updates

2. **userAuthIdentities table**:

   - Has entries for phone, google, facebook
   - `provider + providerId` is unique
   - Foreign keys link to users correctly

3. **userRefreshTokens table**:

   - Token hashes are stored (not plain text)
   - Device info is recorded
   - Expired tokens are cleaned up
   - Only 2 sessions per user max

4. **phoneOtps table**:
   - Should be empty (or have recent pending OTPs)
   - OTP hashes stored (not plain text)
   - Expiry dates are set correctly

---

## 📝 **Postman Collection**

Create this file `postman_collection.json`:

```json
{
  "info": {
    "name": "User Management API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Send OTP",
          "request": {
            "method": "POST",
            "header": [{ "key": "Content-Type", "value": "application/json" }],
            "body": {
              "mode": "raw",
              "raw": "{\"phoneNumber\": \"+919876543210\"}"
            },
            "url": "{{baseUrl}}/api/auth/phone/send-otp"
          }
        },
        {
          "name": "Verify OTP",
          "request": {
            "method": "POST",
            "header": [{ "key": "Content-Type", "value": "application/json" }],
            "body": {
              "mode": "raw",
              "raw": "{\"phoneNumber\": \"+919876543210\", \"otp\": \"123456\", \"deviceInfo\": \"Postman\"}"
            },
            "url": "{{baseUrl}}/api/auth/phone/verify-otp"
          }
        },
        {
          "name": "Refresh Token",
          "request": {
            "method": "POST",
            "header": [{ "key": "Content-Type", "value": "application/json" }],
            "body": {
              "mode": "raw",
              "raw": "{\"refreshToken\": \"{{refreshToken}}\"}"
            },
            "url": "{{baseUrl}}/api/auth/refresh"
          }
        },
        {
          "name": "Logout",
          "request": {
            "method": "POST",
            "header": [{ "key": "Content-Type", "value": "application/json" }],
            "body": {
              "mode": "raw",
              "raw": "{\"refreshToken\": \"{{refreshToken}}\"}"
            },
            "url": "{{baseUrl}}/api/auth/logout"
          }
        }
      ]
    },
    {
      "name": "User",
      "item": [
        {
          "name": "Get Profile",
          "request": {
            "method": "GET",
            "header": [
              { "key": "Authorization", "value": "Bearer {{accessToken}}" }
            ],
            "url": "{{baseUrl}}/api/users/me"
          }
        },
        {
          "name": "Select Role",
          "request": {
            "method": "PATCH",
            "header": [
              { "key": "Authorization", "value": "Bearer {{accessToken}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"role\": \"creator\"}"
            },
            "url": "{{baseUrl}}/api/users/me/role"
          }
        },
        {
          "name": "Update Profile",
          "request": {
            "method": "PATCH",
            "header": [
              { "key": "Authorization", "value": "Bearer {{accessToken}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"fullName\": \"Test User\", \"profilePictureUrl\": \"https://example.com/pic.jpg\"}"
            },
            "url": "{{baseUrl}}/api/users/me"
          }
        }
      ]
    }
  ],
  "variable": [
    { "key": "baseUrl", "value": "http://localhost:5000" },
    { "key": "accessToken", "value": "" },
    { "key": "refreshToken", "value": "" }
  ]
}
```

---

## 🎯 **Next Steps**

Congratulations! 🎉 You've completed the backend for User Management Module 1.

### What's Next:

1. **Deploy to Production**:

   - Set up production database
   - Configure environment variables
   - Enable HTTPS
   - Set up SMS provider (Twilio/AWS SNS)
   - Configure OAuth apps for production URLs

2. **Frontend Development**:

   - Build React components
   - Implement authentication flow
   - Create role selection UI
   - Handle token storage and refresh

3. **Additional Features**:

   - Email verification
   - Two-factor authentication
   - Social profile linking
   - Admin panel

4. **Monitoring & Logging**:
   - Set up error tracking (Sentry)
   - Add analytics
   - Monitor API performance
   - Set up alerts

---

## 📚 **Summary of All Files Created**

```
backend/
├── src/
│   ├── config/
│   │   ├── database.ts          # DB connection helper
│   │   └── passport.ts          # OAuth configuration
│   ├── db/
│   │   ├── schema.ts            # Database schema (Drizzle)
│   │   ├── connection.ts        # DB connection
│   │   └── migrate.ts           # Migration runner
│   ├── middleware/
│   │   ├── auth.middleware.ts   # JWT authentication
│   │   ├── validation.middleware.ts  # Request validation
│   │   ├── error.middleware.ts  # Error handling
│   │   ├── rateLimiter.middleware.ts  # Rate limiting
│   │   ├── logger.middleware.ts # Request logging
│   │   └── index.ts             # Barrel export
│   ├── routes/
│   │   ├── health.routes.ts     # Health check
│   │   ├── phone-auth.routes.ts # Phone auth
│   │   ├── oauth.routes.ts      # OAuth
│   │   ├── token.routes.ts      # Token management
│   │   └── user.routes.ts       # User profile
│   ├── controllers/
│   │   ├── phone-auth.controller.ts
│   │   ├── oauth.controller.ts
│   │   ├── token.controller.ts
│   │   └── user.controller.ts
│   ├── services/
│   │   ├── sms.service.ts       # SMS sending
│   │   ├── auth.service.ts      # Auth logic
│   │   ├── oauth.service.ts     # OAuth logic
│   │   ├── token.service.ts     # Token management
│   │   └── user.service.ts      # User operations
│   ├── utils/
│   │   ├── jwt.ts               # JWT utilities
│   │   ├── hashing.ts           # Bcrypt utilities
│   │   ├── errors.ts            # Custom errors
│   │   ├── phone.ts             # Phone utilities
│   │   └── date.ts              # Date utilities
│   ├── validators/
│   │   └── auth.validators.ts   # Zod schemas
│   ├── app.ts                   # Express app
│   └── server.ts                # Server entry point
├── drizzle.config.ts
├── package.json
├── tsconfig.json
├── .env
├── .env.example
└── .gitignore
```

**Total: 30+ files** ✅

---

**🎉 You're ready to build amazing things!**
