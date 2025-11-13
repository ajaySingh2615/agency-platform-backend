# Step 2: Package Configuration & TypeScript Setup

## 📦 **File 1: package.json**

Create this in your backend root directory:

```json
{
  "name": "agency-platform-backend",
  "version": "1.0.0",
  "description": "Backend for Agency Management System - User Management Module",
  "main": "dist/app.js",
  "scripts": {
    "dev": "tsx watch src/app.ts",
    "build": "tsc",
    "start": "node dist/app.js",
    "db:generate": "drizzle-kit generate:pg",
    "db:push": "drizzle-kit push:pg",
    "db:studio": "drizzle-kit studio",
    "db:migrate": "tsx src/db/migrate.ts"
  },
  "keywords": ["agency", "user-management", "authentication"],
  "author": "Sortout Innovation",
  "license": "MIT",
  "dependencies": {
    "express": "^4.18.2",
    "drizzle-orm": "^0.29.0",
    "postgres": "^3.4.3",
    "zod": "^3.22.4",
    "bcrypt": "^5.1.1",
    "jsonwebtoken": "^9.0.2",
    "dotenv": "^16.3.1",
    "helmet": "^7.1.0",
    "cors": "^2.8.5",
    "express-rate-limit": "^7.1.5",
    "morgan": "^1.10.0",
    "uuid": "^9.0.1",
    "cookie-parser": "^1.4.6"
  },
  "devDependencies": {
    "@types/express": "^4.17.21",
    "@types/node": "^20.10.5",
    "@types/bcrypt": "^5.0.2",
    "@types/jsonwebtoken": "^9.0.5",
    "@types/cors": "^2.8.17",
    "@types/morgan": "^1.9.9",
    "@types/uuid": "^9.0.7",
    "@types/cookie-parser": "^1.4.6",
    "tsx": "^4.7.0",
    "typescript": "^5.3.3",
    "drizzle-kit": "^0.31.6",
    "nodemon": "^3.0.2"
  },
  "overrides": {
    "esbuild": "^0.25.0"
  }
}
```

### 📝 **Important Notes**

- **`drizzle-kit` version**: Updated to `^0.31.6` (latest stable)
- **`overrides` section**: Forces all dependencies to use `esbuild@0.25.0+` to fix security vulnerabilities

### 📝 **Installation Command**

```bash
npm install
```

**Expected Output:**

```
added 245 packages, and audited 245 packages in 20s
found 0 vulnerabilities
```

**Note**: You may see some deprecation warnings (yellow text). These are safe to ignore. See `14_npm_warnings_guide.md` for details.

---

## 📘 **File 2: tsconfig.json**

Create this in your backend root directory:

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "commonjs",
    "lib": ["ES2022"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "moduleResolution": "node",
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": true,
    "allowSyntheticDefaultImports": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist"]
}
```

### 🎯 **Key Settings Explained**

- `strict: true` - Enables all strict type-checking options (security++)
- `noUnusedLocals: true` - Catches unused variables (cleaner code)
- `esModuleInterop: true` - Better compatibility with CommonJS modules
- `sourceMap: true` - Enables debugging with original TypeScript files

---

## 🗄️ **File 3: drizzle.config.ts**

Create this in your backend root directory:

```typescript
import { defineConfig } from "drizzle-kit";
import * as dotenv from "dotenv";

dotenv.config();

export default defineConfig({
  schema: "./src/db/schema.ts",
  out: "./drizzle",
  dialect: "postgresql",
  dbCredentials: {
    url: process.env.DATABASE_URL!,
  },
  verbose: true,
  strict: true,
});
```

### 📝 **What This Does**

- Tells Drizzle where your schema is (`src/db/schema.ts`)
- Specifies where to put migrations (`./drizzle` folder)
- Uses PostgreSQL dialect (for drizzle-kit 0.31.6+)
- Reads database URL from environment variables

### ⚠️ **Important: Breaking Changes in drizzle-kit 0.31.6**

The configuration format has changed from older versions:

| Old API (0.20.x)         | New API (0.31.x)          |
| ------------------------ | ------------------------- |
| `import type { Config }` | `import { defineConfig }` |
| `driver: "pg"`           | `dialect: "postgresql"`   |
| `connectionString`       | `url`                     |
| `} satisfies Config`     | `defineConfig({ ... })`   |

---

## 🔐 **File 4: .env.example**

Create this in your backend root directory (this is a template for other developers):

```env
# =================================================================
# SERVER CONFIGURATION
# =================================================================
NODE_ENV=development
PORT=5000

# =================================================================
# DATABASE (Docker Configuration)
# =================================================================
# Format: postgresql://username:password@host:port/database
# Note: Use 'localhost' when connecting from host machine to Docker container
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/agency_platform

# =================================================================
# JWT SECRETS
# =================================================================
# CRITICAL: Generate these using:
# node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
ACCESS_TOKEN_SECRET=generate_your_own_secret_here
REFRESH_TOKEN_SECRET=generate_your_own_secret_here

# JWT Token Expiry
ACCESS_TOKEN_EXPIRY=15m
REFRESH_TOKEN_EXPIRY=30d

# =================================================================
# OTP CONFIGURATION
# =================================================================
OTP_EXPIRY_MINUTES=5
OTP_LENGTH=6

# =================================================================
# OAUTH PROVIDERS
# =================================================================
# Google OAuth (Get from: https://console.cloud.google.com/)
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
GOOGLE_CALLBACK_URL=http://localhost:5000/api/auth/google/callback

# Facebook OAuth (Get from: https://developers.facebook.com/)
FACEBOOK_APP_ID=your_facebook_app_id_here
FACEBOOK_APP_SECRET=your_facebook_app_secret_here
FACEBOOK_CALLBACK_URL=http://localhost:5000/api/auth/facebook/callback

# =================================================================
# SMS PROVIDER (Example: Twilio)
# =================================================================
# Get from: https://www.twilio.com/console
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Alternative: AWS SNS
# AWS_ACCESS_KEY_ID=your_aws_access_key
# AWS_SECRET_ACCESS_KEY=your_aws_secret_key
# AWS_REGION=us-east-1

# =================================================================
# CORS & SECURITY
# =================================================================
# Comma-separated list of allowed origins
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# =================================================================
# SESSION & DEVICE MANAGEMENT
# =================================================================
MAX_DEVICE_SESSIONS=2

# =================================================================
# RATE LIMITING (requests per window)
# =================================================================
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

---

## 🔒 **File 5: .env (Your Actual File)**

Create this file and add your real secrets. **NEVER commit this to git!**

### 🛡️ How to Generate Secure JWT Secrets

Run this command in your terminal:

```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

Copy the output and paste it as your `ACCESS_TOKEN_SECRET`. Run it again for `REFRESH_TOKEN_SECRET`.

**Example output:**

```
8f2a9b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0d1e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t7u8v9w0x1y2z3a4b5c6
```

---

## 📁 **File 6: .gitignore**

Create this in your backend root directory:

```gitignore
# Dependencies
node_modules/

# Environment variables
.env
.env.local
.env.*.local

# Build output
dist/
build/

# Drizzle migrations (optional - some teams commit these)
drizzle/

# Logs
logs/
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# OS files
.DS_Store
Thumbs.db

# IDE
.vscode/
.idea/
*.swp
*.swo
*~

# Testing
coverage/

# Temporary files
tmp/
temp/
```

---

## ✅ **Verification Checklist**

After creating all these files:

- [ ] Run `npm install` successfully
- [ ] No TypeScript errors when running `npx tsc --noEmit`
- [ ] `.env` file exists with your actual secrets
- [ ] `.gitignore` is in place to prevent committing secrets
- [ ] Docker is installed and running
- [ ] PostgreSQL Docker container is running (`docker-compose ps`)

### 🧪 **Test Your Setup**

Create a test file `src/test.ts`:

```typescript
import * as dotenv from "dotenv";
dotenv.config();

console.log("✅ TypeScript is working!");
console.log("✅ Environment loaded:", process.env.NODE_ENV);
console.log("✅ Database URL exists:", !!process.env.DATABASE_URL);
```

Run it:

```bash
npx tsx src/test.ts
```

You should see:

```
✅ TypeScript is working!
✅ Environment loaded: development
✅ Database URL exists: true
```

---

## 🎯 **Next Step**

Proceed to **`03_database_setup.md`** to implement the schema and connect to PostgreSQL.
