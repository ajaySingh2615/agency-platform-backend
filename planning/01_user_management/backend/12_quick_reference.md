# Quick Reference - User Management API

## 🐳 **Docker Commands**

```bash
# Start PostgreSQL
docker-compose up -d                                                # Start in detached mode

# Check status
docker-compose ps                                                   # View container status

# View logs
docker-compose logs -f postgres                                     # Follow logs in real-time
docker-compose logs --tail=50 postgres                              # View last 50 lines

# Stop/Restart
docker-compose down                                                 # Stop (keeps data)
docker-compose down -v                                              # Stop and delete all data ⚠️
docker-compose restart                                              # Restart containers

# Access database
docker-compose exec postgres psql -U postgres -d agency_platform    # Open psql shell

# Backup & Restore
docker-compose exec postgres pg_dump -U postgres agency_platform > backup.sql   # Backup
docker-compose exec -T postgres psql -U postgres agency_platform < backup.sql   # Restore
```

---

## 🚀 **Server Commands**

```bash
# Development
npm run dev                 # Start dev server with auto-reload

# Database
npm run db:generate         # Generate migration files
npm run db:push            # Push schema to database
npm run db:studio          # Open database GUI
npm run db:migrate         # Run migrations programmatically

# Build & Production
npm run build              # Compile TypeScript
npm start                  # Run production build
```

---

## 🔑 **Generate Secrets**

```bash
# Generate JWT secrets
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"

# Run twice for ACCESS_TOKEN_SECRET and REFRESH_TOKEN_SECRET
```

---

## 📞 **All API Endpoints**

### Health Check

```bash
GET  /health                          # Basic health check
GET  /health/detailed                 # Health + DB status
```

### Phone Authentication

```bash
POST /api/auth/phone/send-otp        # Send OTP
POST /api/auth/phone/verify-otp      # Verify OTP & login/register
```

### OAuth

```bash
GET  /api/auth/google                # Initiate Google login
GET  /api/auth/google/callback       # Google callback
GET  /api/auth/facebook              # Initiate Facebook login
GET  /api/auth/facebook/callback     # Facebook callback
```

### Token Management

```bash
POST   /api/auth/refresh             # Refresh access token
POST   /api/auth/logout              # Logout current device
POST   /api/auth/logout-all          # Logout all devices (auth)
GET    /api/auth/sessions            # Get active sessions (auth)
DELETE /api/auth/sessions/:id        # Revoke session (auth)
```

### User Profile

```bash
GET    /api/users/me                 # Get profile (auth)
GET    /api/users/me/detailed        # Get profile + auth methods (auth)
PATCH  /api/users/me                 # Update profile (auth)
PATCH  /api/users/me/role            # Select role (auth, one-time)
DELETE /api/users/me                 # Delete account (auth)
```

---

## 📋 **Quick Test Sequence**

### 1. Send OTP

```bash
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'
```

### 2. Verify OTP (check console for OTP)

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "PASTE_OTP",
    "deviceInfo": "Test Device"
  }'
```

### 3. Save Tokens

```bash
# Copy accessToken and refreshToken from response
export ACCESS_TOKEN="your_access_token"
export REFRESH_TOKEN="your_refresh_token"
```

### 4. Get Profile

```bash
curl -X GET http://localhost:5000/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 5. Select Role

```bash
curl -X PATCH http://localhost:5000/api/users/me/role \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "creator"}'
```

### 6. Update Access Token (from step 5 response)

```bash
export ACCESS_TOKEN="new_access_token_from_role_response"
```

### 7. Update Profile

```bash
curl -X PATCH http://localhost:5000/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "profilePictureUrl": "https://i.pravatar.cc/150"
  }'
```

---

## 🗄️ **Database Quick Commands**

### PostgreSQL

```bash
# Connect to database
psql -U postgres -d agency_platform

# List tables
\dt

# Query users
SELECT * FROM users;

# Query auth identities
SELECT * FROM user_auth_identities;

# Query sessions
SELECT * FROM user_refresh_tokens;

# Query OTPs (should be empty)
SELECT * FROM phone_otps;
```

### Drizzle Studio

```bash
npm run db:studio
# Opens https://local.drizzle.studio
```

---

## 🔐 **Token Format**

### Access Token (JWT)

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Payload:**

```json
{
  "userId": "uuid",
  "role": "creator" | null,
  "iat": 1234567890,
  "exp": 1234568790,
  "iss": "agency-platform",
  "aud": "agency-platform-users"
}
```

**Expiry:** 15 minutes

### Refresh Token (Random String)

```
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4
```

**Expiry:** 30 days  
**Stored:** Hashed in `user_refresh_tokens` table

---

## ⚠️ **Common Validation Rules**

### Phone Number

- **Format:** E.164 (`+[country][number]`)
- **Example:** `+919876543210`
- **Regex:** `^\+[1-9]\d{1,14}$`

### OTP

- **Length:** 6 digits
- **Format:** Numeric only
- **Example:** `123456`

### Email

- **Format:** Standard email
- **Example:** `user@example.com`

### Role

- **Values:** `creator`, `agency`, `brand`, `gifter`, `admin`
- **Note:** `admin` not shown in UI

### Full Name

- **Min:** 2 characters
- **Max:** 255 characters
- **Allowed:** Letters and spaces only

---

## 🔒 **Rate Limits**

| Endpoint      | Limit        | Window     |
| ------------- | ------------ | ---------- |
| Send OTP      | 3 requests   | 1 hour     |
| Verify OTP    | 5 requests   | 15 minutes |
| Refresh Token | 10 requests  | 1 minute   |
| General API   | 100 requests | 15 minutes |

---

## 🎨 **Response Format**

### Success

```json
{
  "success": true,
  "message": "Optional message",
  "data": {
    // Response data
  }
}
```

### Error

```json
{
  "success": false,
  "message": "Error description"
}
```

### Validation Error

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "body.phoneNumber",
      "message": "Invalid phone number format"
    }
  ]
}
```

---

## 🐛 **HTTP Status Codes**

| Code | Meaning               | When                   |
| ---- | --------------------- | ---------------------- |
| 200  | OK                    | Success                |
| 201  | Created               | Resource created       |
| 400  | Bad Request           | Invalid input          |
| 401  | Unauthorized          | Missing/invalid token  |
| 403  | Forbidden             | No permission          |
| 404  | Not Found             | Resource doesn't exist |
| 409  | Conflict              | Duplicate resource     |
| 429  | Too Many Requests     | Rate limit hit         |
| 500  | Internal Server Error | Server error           |

---

## 📝 **Environment Variables Quick Copy**

```env
NODE_ENV=development
PORT=5000
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/agency_platform
ACCESS_TOKEN_SECRET=generate_with_crypto
REFRESH_TOKEN_SECRET=generate_with_crypto
ACCESS_TOKEN_EXPIRY=15m
REFRESH_TOKEN_EXPIRY=30d
OTP_EXPIRY_MINUTES=5
OTP_LENGTH=6
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_CALLBACK_URL=http://localhost:5000/api/auth/google/callback
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret
FACEBOOK_CALLBACK_URL=http://localhost:5000/api/auth/facebook/callback
FRONTEND_URL=http://localhost:5173
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
MAX_DEVICE_SESSIONS=2
```

---

## 🔍 **Debugging Tips**

### Check Token

```bash
# Decode JWT at https://jwt.io/
# Or use this command:
node -e "console.log(JSON.parse(Buffer.from('PASTE_TOKEN_MIDDLE_PART', 'base64').toString()))"
```

### Check Database

```bash
# Count users
psql -U postgres -d agency_platform -c "SELECT COUNT(*) FROM users;"

# Check recent sessions
psql -U postgres -d agency_platform -c "SELECT * FROM user_refresh_tokens ORDER BY created_at DESC LIMIT 5;"
```

### Check Logs

```bash
# Server logs show:
# - All API requests (via morgan)
# - SMS OTPs (in dev mode)
# - Device limit warnings
# - Errors with stack traces (dev only)
```

---

## 🎯 **Next Steps After Setup**

1. ✅ Test all endpoints with Postman
2. ✅ Verify database schema in Drizzle Studio
3. ✅ Set up production environment variables
4. ✅ Configure SMS provider (Twilio/AWS SNS)
5. ✅ Set up OAuth apps (Google/Facebook)
6. ✅ Deploy to production
7. ✅ Build frontend

---

## 📚 **File Reference**

| Need to...              | See File...                         |
| ----------------------- | ----------------------------------- |
| Understand architecture | `01_initial_setup.md`               |
| Install dependencies    | `02_package_and_config.md`          |
| Set up database         | `03_database_setup.md`              |
| Understand JWT/hashing  | `04_core_utilities.md`              |
| Add middleware          | `05_middleware.md`                  |
| Create health check     | `06_health_check.md`                |
| Implement phone auth    | `07_phone_authentication.md`        |
| Add OAuth               | `08_oauth_implementation.md`        |
| Manage tokens           | `09_token_management.md`            |
| Handle user profiles    | `10_user_profile.md`                |
| Test everything         | `11_testing_guide.md`               |
| Quick reference         | `12_quick_reference.md` (this file) |

---

**Keep this file handy for quick lookups! 🚀**
