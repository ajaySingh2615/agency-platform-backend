# Step 3: Database Schema & Connection Setup

## 🐳 **Prerequisites: Start PostgreSQL with Docker**

Before proceeding, ensure PostgreSQL is running via Docker:

```bash
# Navigate to backend directory (where docker-compose.yml is located)
cd backend

# Start PostgreSQL in detached mode
docker-compose up -d

# Verify it's running
docker-compose ps
# Should show "agency_platform_db" with status "healthy"

# View logs (optional)
docker-compose logs postgres
```

### Docker Database Credentials

The `docker-compose.yml` file configures:

- **Host**: localhost
- **Port**: 5432
- **Username**: postgres
- **Password**: postgres
- **Database**: agency_platform

Your `.env` file should have:

```env
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/agency_platform
```

### Useful Docker Commands

```bash
# Stop PostgreSQL
docker-compose down

# Restart PostgreSQL
docker-compose restart

# Access PostgreSQL shell
docker-compose exec postgres psql -U postgres -d agency_platform

# View database logs
docker-compose logs -f postgres

# Remove everything including data (⚠️ destructive!)
docker-compose down -v
```

---

## 🗄️ **File 1: src/db/schema.ts**

This is your complete database schema using Drizzle ORM:

```typescript
import {
  pgTable,
  uuid,
  varchar,
  text,
  timestamp,
  pgEnum,
  unique,
} from "drizzle-orm/pg-core";
import { relations } from "drizzle-orm";

// =================================================================
// 1. ENUMS (The "Allowed Values")
// =================================================================

/**
 * Defines the strict set of roles a user can have.
 * - 'admin' is for internal use and will not be shown on the signup UI.
 * - All other roles are selectable by users during onboarding.
 */
export const userRoleEnum = pgEnum("user_role", [
  "creator",
  "agency",
  "brand",
  "gifter",
  "admin",
]);

/**
 * Defines the allowed 3rd-party authentication methods.
 */
export const authProviderEnum = pgEnum("auth_provider", [
  "google",
  "facebook",
  "phone",
]);

// =================================================================
// 2. CORE TABLES
// =================================================================

/**
 * [TABLE 1: users]
 * This is the central table for a user's identity.
 * It stores *who* the person is and *what* their role is in the app.
 * It does NOT store how they log in.
 */
export const users = pgTable("users", {
  /**
   * The unique ID for the user. Primary Key.
   */
  id: uuid("id").primaryKey().defaultRandom(),

  /**
   * The user's role.
   * CRITICAL: This is NULL by default when a user first registers.
   * Your application logic MUST check:
   * IF (role IS NULL) THEN redirect user to the "Select Role" screen.
   */
  role: userRoleEnum("role"),

  /**
   * The user's full name, e.g., "Ajay Kumar".
   * Can be pre-filled from Google/Facebook.
   */
  fullName: varchar("full_name", { length: 255 }),

  /**
   * A URL to the user's profile picture.
   * Can be pre-filled from Google/Facebook.
   */
  profilePictureUrl: text("profile_picture_url"),

  /**
   * Timestamp for when this user record was first created.
   */
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),

  /**
   * Timestamp that automatically updates whenever this user's row is changed.
   */
  updatedAt: timestamp("updated_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

/**
 * [TABLE 2: userAuthIdentities]
 * Stores *how* a user logs in.
 * This table links multiple authentication methods (e.g., a Google account AND a phone number)
 * to a single 'users' record.
 */
export const userAuthIdentities = pgTable(
  "user_auth_identities",
  {
    /**
     * The unique ID for this specific auth method. Primary Key.
     */
    id: uuid("id").primaryKey().defaultRandom(),

    /**
     * Foreign Key linking this auth method to the user.
     * If a user is deleted, all their auth methods are deleted too (onDelete: 'cascade').
     */
    userId: uuid("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),

    /**
     * The provider for this auth method (e.g., 'google', 'phone').
     */
    provider: authProviderEnum("provider").notNull(),

    /**
     * The unique ID from that provider.
     * - For Google/Facebook: This is the 'sub' (subject) ID.
     * - For Phone: This is the E.164 formatted phone number (e.g., "+919876543210").
     */
    providerId: text("provider_id").notNull(),

    /**
     * Optional: Stores the email associated with the Google/Facebook login for reference.
     */
    email: varchar("email", { length: 255 }),

    /**
     * Timestamp for when this auth method was first added.
     */
    createdAt: timestamp("created_at", { withTimezone: true })
      .defaultNow()
      .notNull(),
  },
  (table) => {
    // This is the most important constraint in this table.
    // It ensures that no two users can register with the same phone number or Google ID.
    return {
      providerUnique: unique("provider_unique_idx").on(
        table.provider,
        table.providerId
      ),
    };
  }
);

/**
 * [TABLE 3: userRefreshTokens]
 * This table manages active "sessions" and controls logins.
 * It stores the long-lived refresh tokens.
 * This is what enables "logout" and "device limit" features.
 */
export const userRefreshTokens = pgTable("user_refresh_tokens", {
  /**
   * The unique ID for this specific session/token. Primary Key.
   */
  id: uuid("id").primaryKey().defaultRandom(),

  /**
   * Foreign Key linking this session to the user.
   * A user can have multiple rows here (e.g., one for their phone, one for their laptop).
   */
  userId: uuid("user_id")
    .notNull()
    .references(() => users.id, { onDelete: "cascade" }),

  /**
   * SECURITY: We store a HASH (e.g., using bcrypt) of the refresh token.
   * We send the plain token to the user, but only store the hash.
   */
  tokenHash: varchar("token_hash", { length: 255 }).notNull(),

  /**
   * Optional: Store device info (e.g., "Chrome on Windows", "iPhone 15")
   * This allows the user to see a list of their active sessions.
   */
  deviceInfo: text("device_info"),

  /**
   * The timestamp when this refresh token (and session) becomes invalid (e.g., 30 days from now).
   */
  expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),

  /**
   * Timestamp for when this session was created.
   * Used to find the "oldest" session to log out.
   */
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

/**
 * [TABLE 4: phoneOtps]
 * This is a small, temporary, and un-linked table.
 * Its only job is to store OTPs for a few minutes during phone verification.
 * Rows in this table should be deleted after use or when they expire.
 */
export const phoneOtps = pgTable("phone_otps", {
  /**
   * The unique ID for this OTP request. Primary Key.
   */
  id: uuid("id").primaryKey().defaultRandom(),

  /**
   * The phone number the OTP was sent to (e.g., "+919876543210").
   */
  phoneNumber: varchar("phone_number", { length: 50 }).notNull(),

  /**
   * SECURITY: Store a HASH (e.g., using bcrypt) of the 6-digit OTP.
   * Never store the plain OTP.
   */
  otpHash: varchar("otp_hash", { length: 255 }).notNull(),

  /**
   * The timestamp when this OTP becomes invalid (e.g., 5 minutes from now).
   */
  expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),

  /**
   * Timestamp for when this OTP was created.
   */
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

// =================================================================
// 3. RELATIONS (Defines connections for the ORM)
// =================================================================

/**
 * Relations for the 'users' table.
 */
export const usersRelations = relations(users, ({ many }) => ({
  // A user can have MANY auth identities (e.g., Google, Phone)
  authIdentities: many(userAuthIdentities),
  // A user can have MANY refresh tokens (e.g., Laptop, Phone)
  refreshTokens: many(userRefreshTokens),
}));

/**
 * Relations for the 'userAuthIdentities' table.
 */
export const userAuthIdentitiesRelations = relations(
  userAuthIdentities,
  ({ one }) => ({
    // Each auth identity belongs to exactly ONE user
    user: one(users, {
      fields: [userAuthIdentities.userId],
      references: [users.id],
    }),
  })
);

/**
 * Relations for the 'userRefreshTokens' table.
 */
export const userRefreshTokensRelations = relations(
  userRefreshTokens,
  ({ one }) => ({
    // Each refresh token belongs to exactly ONE user
    user: one(users, {
      fields: [userRefreshTokens.userId],
      references: [users.id],
    }),
  })
);
```

---

## 🔌 **File 2: src/db/connection.ts**

This file establishes the connection to PostgreSQL:

```typescript
import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import * as schema from "./schema";

// Get database URL from environment
const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error("DATABASE_URL is not defined in environment variables");
}

// Create PostgreSQL connection
// maxLifetime: 30 seconds (connections are recycled)
// max: 10 (connection pool size)
const client = postgres(connectionString, {
  max: 10,
  idle_timeout: 20,
  connect_timeout: 10,
});

// Create Drizzle ORM instance with schema
export const db = drizzle(client, { schema });

// Export schema for use in other files
export { schema };
```

### 🔍 **What This Does**

- Creates a connection pool (max 10 connections)
- Initializes Drizzle with your schema
- Exports both `db` (for queries) and `schema` (for type safety)

---

## 🚀 **File 3: src/db/migrate.ts**

This file runs migrations programmatically:

```typescript
import { drizzle } from "drizzle-orm/postgres-js";
import { migrate } from "drizzle-orm/postgres-js/migrator";
import postgres from "postgres";
import * as dotenv from "dotenv";

// Load environment variables
dotenv.config();

const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error("DATABASE_URL is not defined");
}

// Create a migration client (separate from main app connection)
const migrationClient = postgres(connectionString, { max: 1 });

const db = drizzle(migrationClient);

async function runMigrations() {
  console.log("⏳ Running migrations...");

  try {
    await migrate(db, { migrationsFolder: "drizzle" });
    console.log("✅ Migrations completed successfully");
  } catch (error) {
    console.error("❌ Migration failed:", error);
    process.exit(1);
  } finally {
    await migrationClient.end();
  }
}

runMigrations();
```

---

## 📝 **File 4: src/config/database.ts**

Database-related configuration and helper functions:

```typescript
import { db } from "../db/connection";
import { sql } from "drizzle-orm";

/**
 * Check if database connection is healthy
 */
export async function checkDatabaseConnection(): Promise<boolean> {
  try {
    // Simple query to test connection
    await db.execute(sql`SELECT 1`);
    return true;
  } catch (error) {
    console.error("Database connection failed:", error);
    return false;
  }
}

/**
 * Clean up expired OTPs (should be run periodically)
 */
export async function cleanupExpiredOtps(): Promise<void> {
  const { phoneOtps } = await import("../db/schema");
  try {
    await db.delete(phoneOtps).where(sql`${phoneOtps.expiresAt} < NOW()`);
    console.log("✅ Expired OTPs cleaned up");
  } catch (error) {
    console.error("❌ Failed to cleanup OTPs:", error);
  }
}

/**
 * Clean up expired refresh tokens (should be run periodically)
 */
export async function cleanupExpiredTokens(): Promise<void> {
  const { userRefreshTokens } = await import("../db/schema");
  try {
    await db
      .delete(userRefreshTokens)
      .where(sql`${userRefreshTokens.expiresAt} < NOW()`);
    console.log("✅ Expired refresh tokens cleaned up");
  } catch (error) {
    console.error("❌ Failed to cleanup tokens:", error);
  }
}

/**
 * Run all cleanup tasks
 */
export async function runCleanupTasks(): Promise<void> {
  await Promise.all([cleanupExpiredOtps(), cleanupExpiredTokens()]);
}
```

---

## 🛠️ **Setup & Migration Commands**

### Step 0: Ensure Docker PostgreSQL is Running

```bash
# Start if not already running
docker-compose up -d

# Verify
docker-compose ps
```

### Step 1: Generate SQL Migrations

After creating the schema, generate migration files:

```bash
npm run db:generate
```

This creates SQL files in the `drizzle/` folder.

### Step 2: Apply Migrations to Database

```bash
npm run db:migrate
```

This runs the SQL migrations on your database.

### Alternative: Push Schema Directly (Development Only)

For rapid development, you can push schema changes directly:

```bash
npm run db:push
```

⚠️ **Warning**: This bypasses migrations. Only use in development!

### Step 3: Open Drizzle Studio (Optional)

Visual database browser:

```bash
npm run db:studio
```

Opens at `https://local.drizzle.studio`

---

## ✅ **Verification**

### Test Database Connection

Create `src/test-db.ts`:

```typescript
import * as dotenv from "dotenv";
dotenv.config();

import { checkDatabaseConnection } from "./config/database";
import { db } from "./db/connection";
import { users } from "./db/schema";

async function testDatabase() {
  console.log("🔍 Testing database connection...");

  const isConnected = await checkDatabaseConnection();

  if (!isConnected) {
    console.error("❌ Database connection failed");
    process.exit(1);
  }

  console.log("✅ Database connection successful");

  // Try to query users table
  try {
    const allUsers = await db.select().from(users);
    console.log(`✅ Users table exists. Current count: ${allUsers.length}`);
  } catch (error) {
    console.error("❌ Failed to query users table:", error);
  }

  process.exit(0);
}

testDatabase();
```

Run it:

```bash
npx tsx src/test-db.ts
```

Expected output:

```
🔍 Testing database connection...
✅ Database connection successful
✅ Users table exists. Current count: 0
```

---

## 🔒 **Security Notes**

1. **Connection Pooling**: Limits concurrent connections to prevent database overload
2. **Parameterized Queries**: Drizzle automatically uses prepared statements (SQL injection protection)
3. **Cascading Deletes**: When a user is deleted, all their auth identities and tokens are automatically removed
4. **Unique Constraints**: The `provider + providerId` constraint prevents duplicate accounts

---

## 📊 **Database Schema Visual**

```
┌─────────────┐
│   users     │
│─────────────│
│ id (PK)     │◄─────┐
│ role        │      │
│ fullName    │      │
│ profilePic  │      │
│ createdAt   │      │
│ updatedAt   │      │
└─────────────┘      │
                     │
        ┌────────────┴──────────────┐
        │                           │
┌───────┴────────────┐    ┌─────────┴────────────┐
│userAuthIdentities  │    │ userRefreshTokens    │
│────────────────────│    │──────────────────────│
│ id (PK)            │    │ id (PK)              │
│ userId (FK)        │    │ userId (FK)          │
│ provider           │    │ tokenHash            │
│ providerId (UNIQUE)│    │ deviceInfo           │
│ email              │    │ expiresAt            │
│ createdAt          │    │ createdAt            │
└────────────────────┘    └──────────────────────┘

┌─────────────────┐
│   phoneOtps     │ (Temporary, not linked)
│─────────────────│
│ id (PK)         │
│ phoneNumber     │
│ otpHash         │
│ expiresAt       │
│ createdAt       │
└─────────────────┘
```

---

## 🎯 **Next Step**

Proceed to **`04_core_utilities.md`** to create JWT, hashing, and error handling utilities.
