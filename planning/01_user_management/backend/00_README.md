# User Management Module - Complete Backend Guide

## 📚 **Quick Navigation**

This folder contains a **complete, production-ready backend implementation** for the User Management module of your Agency Management System.

### 📖 **Read Files in This Order:**

| File                            | Purpose                                         | Time to Read |
| ------------------------------- | ----------------------------------------------- | ------------ |
| `01_initial_setup.md`           | Architecture, tech stack, security principles   | 10 min       |
| `02_package_and_config.md`      | Dependencies, TypeScript, environment setup     | 10 min       |
| `03_database_setup.md`          | Schema, migrations, database connection         | 15 min       |
| `04_core_utilities.md`          | JWT, hashing, error handling, validation        | 15 min       |
| `05_middleware.md`              | Auth, validation, error handling, rate limiting | 15 min       |
| `06_health_check.md`            | First API route & Express app setup             | 10 min       |
| `07_phone_authentication.md`    | Phone OTP authentication                        | 20 min       |
| `08_oauth_implementation.md`    | Google & Facebook OAuth                         | 20 min       |
| `09_token_management.md`        | Refresh tokens, logout, sessions                | 15 min       |
| `10_user_profile.md`            | Profile management, role selection              | 15 min       |
| `11_testing_guide.md`           | Complete testing & security checklist           | 30 min       |
| `12_quick_reference.md`         | Quick reference for commands and endpoints      | 5 min        |
| `13_docker_guide.md`            | Complete Docker setup and troubleshooting       | 15 min       |
| `14_npm_warnings_guide.md`      | NPM warnings and security vulnerability guide   | 10 min       |
| `15_drizzle_migration_guide.md` | Drizzle-kit 0.31.6 breaking changes explained   | 5 min        |

**Total Reading Time: ~3.75 hours**  
**Implementation Time: 6-8 hours**

---

## 🚀 **Quick Start (TL;DR)**

### 1. Prerequisites

Ensure you have:

- Node.js ≥ 18.x
- Docker Desktop installed and running

### 2. Setup Backend

```bash
cd backend
npm install
cp .env.example .env
# Edit .env with your database URL and secrets
```

### 3. Start PostgreSQL with Docker

```bash
# Start PostgreSQL container in detached mode
docker-compose up -d

# Verify it's running
docker-compose ps
# Should show "agency_platform_db" with status "healthy"
```

### 4. Generate JWT Secrets

```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
# Run twice, paste into .env as ACCESS_TOKEN_SECRET and REFRESH_TOKEN_SECRET
```

### 5. Database Migration

```bash
npm run db:generate  # Generate migrations
npm run db:push      # Apply to database
```

### 6. Start Server

```bash
npm run dev
```

### 7. Test

```bash
curl http://localhost:5000/health
# Should return: {"success": true, "message": "Server is running", ...}
```

---

## 📦 **What's Included**

### ✅ **Authentication Methods**

- ✅ Phone OTP (SMS-based)
- ✅ Google OAuth 2.0
- ✅ Facebook OAuth 2.0
- ✅ JWT (Access + Refresh Token pattern)

### ✅ **Security Features**

- ✅ bcrypt hashing (OTPs, refresh tokens)
- ✅ Rate limiting (prevents brute force)
- ✅ Input validation (Zod schemas)
- ✅ SQL injection prevention (Drizzle ORM)
- ✅ CORS configuration
- ✅ Helmet security headers
- ✅ Device limit (max 2 sessions)

### ✅ **User Features**

- ✅ User registration (phone/OAuth)
- ✅ User login (existing users)
- ✅ Role selection (creator, agency, brand, gifter)
- ✅ Profile management (name, picture)
- ✅ Session management (view/revoke devices)
- ✅ Account deletion

### ✅ **API Endpoints**

#### Health

- `GET /health` - Basic health check
- `GET /health/detailed` - Detailed health with DB status

#### Phone Authentication

- `POST /api/auth/phone/send-otp` - Send OTP to phone
- `POST /api/auth/phone/verify-otp` - Verify OTP & login/register

#### OAuth

- `GET /api/auth/google` - Initiate Google OAuth
- `GET /api/auth/google/callback` - Google OAuth callback
- `GET /api/auth/facebook` - Initiate Facebook OAuth
- `GET /api/auth/facebook/callback` - Facebook OAuth callback

#### Token Management

- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout from current device
- `POST /api/auth/logout-all` - Logout from all devices (requires auth)
- `GET /api/auth/sessions` - Get active sessions (requires auth)
- `DELETE /api/auth/sessions/:id` - Revoke specific session (requires auth)

#### User Profile

- `GET /api/users/me` - Get current user profile (requires auth)
- `GET /api/users/me/detailed` - Get profile with auth methods (requires auth)
- `PATCH /api/users/me` - Update profile (requires auth)
- `PATCH /api/users/me/role` - Select role (requires auth, one-time)
- `DELETE /api/users/me` - Delete account (requires auth)

**Total: 17 endpoints** ✅

---

## 🗄️ **Database Schema**

### Tables

1. **users** - Core user identity

   - `id`, `role`, `fullName`, `profilePictureUrl`, `createdAt`, `updatedAt`

2. **userAuthIdentities** - Authentication methods

   - `id`, `userId`, `provider`, `providerId`, `email`, `createdAt`
   - Unique: `provider + providerId`

3. **userRefreshTokens** - Active sessions

   - `id`, `userId`, `tokenHash`, `deviceInfo`, `expiresAt`, `createdAt`

4. **phoneOtps** - Temporary OTP storage
   - `id`, `phoneNumber`, `otpHash`, `expiresAt`, `createdAt`

---

## 🔐 **Security Best Practices Implemented**

### ✅ Token Security

- Access tokens: 15 minutes (stateless JWT)
- Refresh tokens: 30 days (stored in DB, can be revoked)
- Tokens include issuer and audience claims
- Refresh tokens are hashed before storage

### ✅ OTP Security

- OTPs are 6-digit numeric codes
- OTPs expire in 5 minutes
- OTPs are hashed with bcrypt
- OTPs are deleted after use
- Rate limited to 3 requests/hour per IP

### ✅ Session Security

- Max 2 devices per user
- Device info tracked
- Sessions can be revoked individually or all at once
- Expired sessions cleaned up automatically

### ✅ Input Validation

- All inputs validated with Zod
- Phone numbers normalized to E.164
- Email and URL formats validated
- SQL injection prevented (parameterized queries)

### ✅ Rate Limiting

- OTP send: 3/hour
- OTP verify: 5/15min
- Token refresh: 10/min
- General API: 100/15min

---

## 🎯 **Usage Examples**

### Phone Registration Flow

```javascript
// 1. Send OTP
POST /api/auth/phone/send-otp
Body: { "phoneNumber": "+919876543210" }
Response: { "success": true, "message": "OTP sent" }

// 2. Verify OTP
POST /api/auth/phone/verify-otp
Body: {
  "phoneNumber": "+919876543210",
  "otp": "123456",
  "deviceInfo": "iPhone 15"
}
Response: {
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": { "id": "...", "role": null },
    "isNewUser": true
  }
}

// 3. Select Role
PATCH /api/users/me/role
Headers: { "Authorization": "Bearer ACCESS_TOKEN" }
Body: { "role": "creator" }
Response: {
  "success": true,
  "data": {
    "user": { "role": "creator" },
    "accessToken": "NEW_TOKEN_WITH_ROLE"
  }
}

// 4. Update Profile
PATCH /api/users/me
Headers: { "Authorization": "Bearer ACCESS_TOKEN" }
Body: {
  "fullName": "John Doe",
  "profilePictureUrl": "https://example.com/pic.jpg"
}
Response: { "success": true, "data": { "user": {...} } }
```

---

## 📊 **File Structure Overview**

```
backend/
├── src/
│   ├── config/           # App configuration
│   ├── db/               # Database schema & connection
│   ├── middleware/       # Express middleware
│   ├── routes/           # API routes
│   ├── controllers/      # Route handlers
│   ├── services/         # Business logic
│   ├── utils/            # Helper functions
│   ├── validators/       # Zod schemas
│   ├── app.ts            # Express app
│   └── server.ts         # Entry point
├── drizzle/              # Generated migrations
├── drizzle.config.ts     # Drizzle configuration
├── package.json          # Dependencies
├── tsconfig.json         # TypeScript config
├── .env                  # Environment variables (git-ignored)
└── .env.example          # Template for .env
```

---

## 🧪 **Testing**

### Manual Testing

See `11_testing_guide.md` for comprehensive testing flows covering:

- New user registration
- Existing user login
- Token refresh
- Logout
- Profile updates
- Role selection
- Error handling
- Rate limiting
- Edge cases

### Automated Testing (Future)

Consider adding:

- Jest for unit tests
- Supertest for API tests
- Test coverage reports

---

## 🚀 **Deployment Checklist**

### Pre-Deployment

- [ ] All tests pass
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] JWT secrets are strong and unique
- [ ] CORS configured for production domains
- [ ] Rate limits are appropriate
- [ ] Error logging is set up
- [ ] HTTPS is enforced

### Production Services Needed

1. **Database**: PostgreSQL 14+ (AWS RDS, DigitalOcean, etc.)
2. **SMS Provider**: Twilio, AWS SNS, or similar
3. **OAuth Apps**: Google Cloud Console, Facebook Developers
4. **Server**: Any Node.js hosting (AWS, Heroku, Railway, etc.)
5. **Error Tracking**: Sentry (optional but recommended)

---

## 📝 **Environment Variables Required**

```env
# Server
NODE_ENV=production
PORT=5000

# Database
DATABASE_URL=postgresql://...

# JWT (64+ character random strings)
ACCESS_TOKEN_SECRET=...
REFRESH_TOKEN_SECRET=...
ACCESS_TOKEN_EXPIRY=15m
REFRESH_TOKEN_EXPIRY=30d

# OTP
OTP_EXPIRY_MINUTES=5

# OAuth
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GOOGLE_CALLBACK_URL=https://yourdomain.com/api/auth/google/callback

FACEBOOK_APP_ID=...
FACEBOOK_APP_SECRET=...
FACEBOOK_CALLBACK_URL=https://yourdomain.com/api/auth/facebook/callback

# SMS (Twilio example)
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=+1234567890

# CORS
ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Session
MAX_DEVICE_SESSIONS=2
```

---

## 🐛 **Common Issues & Solutions**

### "Database connection failed"

- Check Docker container is running: `docker-compose ps`
- Start PostgreSQL if stopped: `docker-compose up -d`
- Verify DATABASE_URL is correct in `.env`
- Check Docker logs: `docker-compose logs postgres`
- Ensure port 5432 is not already in use

### "Invalid token"

- Check JWT secrets are set in .env
- Ensure token isn't expired
- Verify Authorization header format: `Bearer <token>`

### "Too many requests"

- Rate limit hit - wait or adjust limits
- Different endpoints have different limits
- Check `rateLimiter.middleware.ts`

### "OTP not received"

- Check console logs in development mode
- Verify SMS provider credentials in production
- Check phone number format (E.164)

---

## 📚 **Additional Resources**

### Documentation

- [Drizzle ORM Docs](https://orm.drizzle.team/)
- [Express.js Guide](https://expressjs.com/)
- [JWT.io](https://jwt.io/) - Decode and verify JWTs
- [Zod Documentation](https://zod.dev/)

### Tools

- [Postman](https://www.postman.com/) - API testing
- [Drizzle Studio](https://orm.drizzle.team/drizzle-studio/overview) - Database GUI
- [Thunder Client](https://www.thunderclient.com/) - VS Code extension for API testing

---

## 🎓 **Learning Path**

If you're new to this stack:

1. **Week 1**: Read files 01-06, understand architecture
2. **Week 2**: Implement phone authentication (file 07)
3. **Week 3**: Add OAuth and token management (files 08-09)
4. **Week 4**: Complete profile management and testing (files 10-11)

---

## 🤝 **Support & Contribution**

### Questions?

- Review the relevant guide file
- Check the testing guide for examples
- Verify environment variables are correct

### Want to Extend?

- Email verification
- Two-factor authentication
- Admin panel
- Audit logs
- Analytics

---

## ✅ **Module 1 Complete!**

You now have:

- ✅ Secure authentication system
- ✅ User management
- ✅ Session handling
- ✅ Role-based access
- ✅ Production-ready code
- ✅ Comprehensive documentation

**Next**: Build the frontend (React + Vite + Tailwind)

---

## 📄 **License**

This code is provided as a learning resource and template. Adapt it to your needs!

---

**Happy Coding! 🚀**

_Last Updated: November 2024_
