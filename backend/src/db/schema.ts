import {
  PgTable,
  uuid,
  varchar,
  text,
  timestamp,
  pgEnum,
  unique,
  pgTable,
} from "drizzle-orm/pg-core";
import { relations } from "drizzle-orm";
import { table } from "console";

// 1. Enums (The "allowed values")
export const userRoleEnum = pgEnum("user_role", [
  "creator",
  "agency",
  "brand",
  "gifter",
  "admin",
]);

// 2. Defines the allowed 3rd-party authentication methods
export const authProviderEnum = pgEnum("auth_provider", [
  "google",
  "facebook",
  "phone",
]);

// core tables

// table 1: user
export const users = pgTable("users", {
  id: uuid("id").primaryKey().defaultRandom(),
  role: userRoleEnum("role"),
  fullName: varchar("full_name", { length: 255 }),
  profilePictureUrl: text("profile_picture_url"),
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
  updatedAt: timestamp("updated_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

// table 2: userAuthIdentities
export const userAuthIdentities = pgTable(
  "user_auth_identities",
  {
    // The unique ID for this specific auth method. Primary Key.
    id: uuid("id").primaryKey().defaultRandom(),

    // Foreign Key linking this auth method to the user. If a user is deleted, all their auth methods are deleted too (onDelete: 'cascade').
    userId: uuid("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),

    // The provider for this auth method (e.g., 'google', 'phone').
    provider: authProviderEnum("provider").notNull(),

    // The unique ID from that provider. For Google/Facebook: This is the 'sub' (subject) ID. For Phone: This is the E.164 formatted phone number (e.g., "+919876543210")
    providerId: text("provider_id").notNull(),

    // Optional: Stores the email associated with the Google/Facebook login for reference.
    email: varchar("email", { length: 255 }),

    // Timestamp for when this auth method was first added.
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

// [TABLE 3: userRefreshTokens]
// This table manages active "sessions" and controls logins.
// It stores the long-lived refresh tokens.
// This is what enables "logout" and "device limit" features

export const userRefreshTokens = pgTable("user_refresh_tokens", {
  // The unquie id for this specific session/token.
  // Primary key
  id: uuid("id").primaryKey().defaultRandom(),

  // Foreign key linking this session to the user.
  // A user can have multiple rows here (e.g., one for their phone, one for their laptop)
  userId: uuid("user_id")
    .notNull()
    .references(() => users.id, { onDelete: "cascade" }),

  // SECURITY: We store a HASH (e.g., using bcrypt) of the refresh token.
  // We send the plain token to the user, but only store the hash
  tokenHash: varchar("token_hash", { length: 225 }).notNull(),

  // Optional: Store device info (e.g., "Chrome on windows", "iphone 15")
  // This allows the user to see a list of thier active sessions
  deviceInfo: text("device_info"),

  // The timestamp when this refresh token (and session) becomes invalid (e.g., 30 days from now).
  expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),

  // Timestamp for when this session was created. Used to find the "oldest" session to log out.
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

// [TABLE 4: PhoneOtps]
// This is a small, temporary, and un-linked table. Its only job is to store OTPs for a few minutes during phone verification. Rows in this table should be deleted after use or when they expire.
export const phoneOtps = pgTable("phone_otps", {
  // The unique ID for the OTP request. Primary key
  id: uuid("id").primaryKey().defaultRandom(),

  // The phone number the OTP was sent to (e.g., +919876543210)
  phoneNumber: varchar("phone_number", { length: 50 }).notNull(),

  // SECURITY: Store a HASH (e.g., using bcrypt ) of the 6-digit OTP.
  // Never share the plain OTP
  otpHash: varchar("otp_hash", { length: 255 }).notNull(),

  // The timestamp when this OTP becomes invalid (e.g., 5 minutes from now)
  expiresAt: timestamp("expires_at", {
    withTimezone: true,
  }).notNull(),

  // Timestamp for when this OTP was created.
  createdAt: timestamp("created_at", { withTimezone: true })
    .defaultNow()
    .notNull(),
});

// =================================================================
// 3. RELATIONS (Defines connections for the ORM)
// =================================================================

// Relations for the users table
export const usersRelations = relations(users, ({ many }) => ({
  // A user can have MANY auth identities (e.g., Google, Phone)
  authIdentities: many(userAuthIdentities),

  // A user can have MANY refresh tokens (e.g., Laptop, Phone)
  refreshTokens: many(userRefreshTokens),
}));

// Relations for the 'userAuthIdentities' table
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

// Relations for the 'userAuthIdentitiesRelations' table
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
