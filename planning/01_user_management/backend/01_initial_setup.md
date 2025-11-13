# Module 1: User Management - Initial Setup Guide

## 📚 **Step 1: Understanding the Architecture**

### Tech Stack

- **Backend Framework**: Express.js (Node.js)
- **Database**: PostgreSQL 14+
- **ORM**: Drizzle ORM
- **Language**: TypeScript
- **Authentication**: JWT (Access + Refresh Token Pattern)
- **Validation**: Zod
- **Security**: bcrypt, helmet, rate-limiting

### Project Structure

```
backend/
├── src/
│   ├── config/              # Environment & app configuration
│   ├── db/                  # Database schema, migrations, connection
│   ├── middleware/          # Express middleware (auth, validation, error)
│   ├── routes/              # API route definitions
│   ├── controllers/         # Business logic
│   ├── services/            # Database operations
│   ├── utils/               # Helper functions (JWT, hashing, etc.)
│   ├── validators/          # Zod schemas for request validation
│   └── app.ts               # Express app setup
├── drizzle.config.ts        # Drizzle configuration
├── package.json
├── tsconfig.json
└── .env.example
```

---

## 📦 **Step 2: Required Libraries**

### Core Dependencies

| Library              | Purpose                   | Security Benefit                                   |
| -------------------- | ------------------------- | -------------------------------------------------- |
| `express`            | Web framework             | Industry standard, well-maintained                 |
| `typescript`         | Type safety               | Prevents type-related bugs                         |
| `drizzle-orm`        | ORM                       | SQL injection prevention via parameterized queries |
| `postgres`           | PostgreSQL driver         | Direct connection to database                      |
| `zod`                | Schema validation         | Input validation & sanitization                    |
| `bcrypt`             | Password/token hashing    | One-way encryption for sensitive data              |
| `jsonwebtoken`       | JWT creation/verification | Stateless authentication                           |
| `dotenv`             | Environment variables     | Keeps secrets out of code                          |
| `helmet`             | Security headers          | Protection against common web vulnerabilities      |
| `cors`               | CORS management           | Controlled cross-origin access                     |
| `express-rate-limit` | Rate limiting             | Prevents brute force attacks                       |
| `express-validator`  | Additional validation     | Extra layer of input sanitization                  |
| `morgan`             | HTTP request logger       | Audit trail for debugging                          |
| `uuid`               | Unique ID generation      | Secure random IDs                                  |

### Dev Dependencies

| Library                               | Purpose                              |
| ------------------------------------- | ------------------------------------ |
| `tsx`                                 | TypeScript execution for development |
| `@types/node`, `@types/express`, etc. | TypeScript type definitions          |
| `drizzle-kit`                         | Database migration tool              |
| `nodemon`                             | Auto-restart on file changes         |

### Optional But Recommended

- `twilio` or `aws-sns` - For sending OTP SMS
- `passport` - For OAuth (Google/Facebook)
- `passport-google-oauth20` - Google OAuth strategy
- `passport-facebook` - Facebook OAuth strategy
- `winston` - Advanced logging

---

## 🔐 **Step 3: Security Principles**

### 1. **Never Store Plain Text**

- ❌ Never store plain OTP, passwords, or refresh tokens
- ✅ Always use bcrypt with salt rounds ≥ 12

### 2. **JWT Best Practices**

- **Access Token**: Short-lived (15 minutes), stored in memory (not localStorage)
- **Refresh Token**: Long-lived (30 days), httpOnly cookie or secure storage
- Include only essential data in JWT payload: `userId`, `role`

### 3. **Input Validation**

- Validate EVERY request with Zod schemas
- Sanitize phone numbers to E.164 format (`+919876543210`)
- Validate email format for OAuth

### 4. **Rate Limiting**

| Endpoint                     | Limit                       | Reason              |
| ---------------------------- | --------------------------- | ------------------- |
| `/api/auth/phone/send-otp`   | 3 requests/hour per phone   | Prevent SMS spam    |
| `/api/auth/phone/verify-otp` | 5 attempts/15 min per phone | Prevent brute force |
| `/api/auth/refresh`          | 10 requests/min per IP      | Prevent token abuse |
| All other routes             | 100 requests/15 min per IP  | General protection  |

### 5. **Database Security**

- Use connection pooling
- Never expose database errors to client
- Use transactions for multi-step operations (e.g., creating user + auth identity)

---

## 🚀 **Step 4: Development Flow**

### Order of Implementation

1. **Health Check Route** (Verify server is running)
2. **Database Setup** (Schema, migrations, connection)
3. **Utilities** (JWT, hashing, error handling)
4. **Phone Auth** (Send OTP → Verify OTP → Issue Tokens)
5. **OAuth** (Google/Facebook login)
6. **Token Management** (Refresh, Logout)
7. **User Profile** (Get profile, Update profile, Select role)

### Testing Each Step

- Use **Postman** or **Thunder Client** (VS Code extension)
- Test happy path AND error cases
- Verify tokens using [jwt.io](https://jwt.io)

---

## ⚠️ **Step 5: Common Pitfalls to Avoid**

1. **Token Storage on Client**

   - ❌ Don't store refresh tokens in localStorage (vulnerable to XSS)
   - ✅ Use httpOnly cookies OR secure mobile storage

2. **OTP Expiry**

   - Set OTPs to expire in 5 minutes
   - Delete used/expired OTPs from database

3. **Device Limit Logic**

   - Implement in application code, not database constraints
   - Always delete oldest session when limit reached

4. **Error Messages**
   - ❌ "Invalid password" (reveals username exists)
   - ✅ "Invalid credentials" (generic message)

---

## 📝 **Step 6: Environment Variables**

You'll need these in your `.env` file:

```env
# Server
NODE_ENV=development
PORT=5000

# Database
DATABASE_URL=postgresql://username:password@localhost:5432/agency_platform

# JWT Secrets (generate using: node -e "console.log(require('crypto').randomBytes(64).toString('hex'))")
ACCESS_TOKEN_SECRET=your_access_token_secret_here
REFRESH_TOKEN_SECRET=your_refresh_token_secret_here

# JWT Expiry
ACCESS_TOKEN_EXPIRY=15m
REFRESH_TOKEN_EXPIRY=30d

# OTP Configuration
OTP_EXPIRY_MINUTES=5

# OAuth (Get from Google Cloud Console & Facebook Developers)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_CALLBACK_URL=http://localhost:5000/api/auth/google/callback

FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_APP_SECRET=your_facebook_app_secret
FACEBOOK_CALLBACK_URL=http://localhost:5000/api/auth/facebook/callback

# SMS Provider (Example: Twilio)
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=your_twilio_phone_number

# CORS
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Rate Limiting
MAX_DEVICE_SESSIONS=2
```

---

## 🐳 **Step 7: Docker Setup (PostgreSQL)**

We'll use Docker to run PostgreSQL, which makes setup easier and ensures consistency across environments.

### Prerequisites

- Docker Desktop installed ([Download here](https://www.docker.com/products/docker-desktop))
- Docker Compose (included with Docker Desktop)

### Quick Start

The `docker-compose.yml` file is included in the backend folder. It configures:

- **PostgreSQL 14** (Alpine Linux - lightweight)
- **Port**: 5432 (mapped to host)
- **Credentials**: postgres/postgres (change in production!)
- **Database**: agency_platform
- **Data Persistence**: Volume mounted for data persistence
- **Health Check**: Automatic database health monitoring

### Commands

```bash
# Start PostgreSQL in detached mode
docker-compose up -d

# Check if container is running
docker-compose ps

# View logs
docker-compose logs postgres

# Stop PostgreSQL
docker-compose down

# Stop and remove all data (⚠️ destructive!)
docker-compose down -v
```

### Verify Database is Running

```bash
# Option 1: Using docker-compose
docker-compose ps
# Should show "healthy" status

# Option 2: Connect with psql
docker-compose exec postgres psql -U postgres -d agency_platform
# Type \q to exit
```

---

## ✅ **Step 8: Pre-Development Checklist**

Before writing code, ensure:

- [ ] Docker Desktop is installed and running
- [ ] PostgreSQL container is running (`docker-compose up -d`)
- [ ] Node.js version ≥ 18.x installed
- [ ] You have a code editor (VS Code recommended)
- [ ] You understand JWT flow (access + refresh tokens)
- [ ] You have a plan for SMS provider (Twilio/AWS SNS/other)
- [ ] You've read through the schema explanation

---

## 🎯 **Next Steps**

The following files will guide you through implementation:

- `docker-compose.yml` - PostgreSQL Docker configuration (already created!)
- `02_package_and_config.md` - package.json, tsconfig, and configuration files
- `03_database_setup.md` - Schema implementation and migrations
- `04_core_utilities.md` - JWT, hashing, error handling
- `05_middleware.md` - Auth, validation, error handling middleware
- `06_health_check.md` - First API route
- `07_phone_authentication.md` - Phone authentication implementation
- `08_oauth_implementation.md` - Google/Facebook login
- `09_token_management.md` - Refresh and logout
- `10_user_profile.md` - Profile management and role selection
- `11_testing_guide.md` - How to test everything

---

**Ready to proceed? Start Docker and then move to file 02! 🚀**
