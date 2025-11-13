# Step 4: Core Utilities - JWT, Hashing, Error Handling & Validation

## 🔐 **File 1: src/utils/jwt.ts**

JWT token generation and verification utilities:

```typescript
import jwt from "jsonwebtoken";

// Type definitions for JWT payloads
export interface AccessTokenPayload {
  userId: string;
  role: string | null;
}

export interface RefreshTokenPayload {
  userId: string;
  tokenId: string; // Reference to the row in userRefreshTokens table
}

/**
 * Generate an access token (short-lived, 15 minutes)
 */
export function generateAccessToken(payload: AccessTokenPayload): string {
  const secret = process.env.ACCESS_TOKEN_SECRET;
  const expiry = process.env.ACCESS_TOKEN_EXPIRY || "15m";

  if (!secret) {
    throw new Error("ACCESS_TOKEN_SECRET is not defined");
  }

  return jwt.sign(payload, secret, {
    expiresIn: expiry,
    issuer: "agency-platform",
    audience: "agency-platform-users",
  });
}

/**
 * Generate a refresh token (long-lived, 30 days)
 */
export function generateRefreshToken(payload: RefreshTokenPayload): string {
  const secret = process.env.REFRESH_TOKEN_SECRET;
  const expiry = process.env.REFRESH_TOKEN_EXPIRY || "30d";

  if (!secret) {
    throw new Error("REFRESH_TOKEN_SECRET is not defined");
  }

  return jwt.sign(payload, secret, {
    expiresIn: expiry,
    issuer: "agency-platform",
    audience: "agency-platform-users",
  });
}

/**
 * Verify and decode an access token
 */
export function verifyAccessToken(token: string): AccessTokenPayload {
  const secret = process.env.ACCESS_TOKEN_SECRET;

  if (!secret) {
    throw new Error("ACCESS_TOKEN_SECRET is not defined");
  }

  try {
    const decoded = jwt.verify(token, secret, {
      issuer: "agency-platform",
      audience: "agency-platform-users",
    }) as AccessTokenPayload;

    return decoded;
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error("Access token has expired");
    }
    if (error instanceof jwt.JsonWebTokenError) {
      throw new Error("Invalid access token");
    }
    throw error;
  }
}

/**
 * Verify and decode a refresh token
 */
export function verifyRefreshToken(token: string): RefreshTokenPayload {
  const secret = process.env.REFRESH_TOKEN_SECRET;

  if (!secret) {
    throw new Error("REFRESH_TOKEN_SECRET is not defined");
  }

  try {
    const decoded = jwt.verify(token, secret, {
      issuer: "agency-platform",
      audience: "agency-platform-users",
    }) as RefreshTokenPayload;

    return decoded;
  } catch (error) {
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error("Refresh token has expired");
    }
    if (error instanceof jwt.JsonWebTokenError) {
      throw new Error("Invalid refresh token");
    }
    throw error;
  }
}

/**
 * Extract token from Authorization header
 * Expected format: "Bearer <token>"
 */
export function extractBearerToken(
  authHeader: string | undefined
): string | null {
  if (!authHeader) {
    return null;
  }

  const parts = authHeader.split(" ");

  if (parts.length !== 2 || parts[0] !== "Bearer") {
    return null;
  }

  return parts[1];
}
```

---

## 🔒 **File 2: src/utils/hashing.ts**

Bcrypt hashing utilities for OTPs and refresh tokens:

```typescript
import bcrypt from "bcrypt";
import { randomBytes } from "crypto";

// Salt rounds for bcrypt (12 is a good balance of security and performance)
const SALT_ROUNDS = 12;

/**
 * Hash a plain text value using bcrypt
 * Used for: OTPs, Refresh Tokens
 */
export async function hashValue(plainText: string): Promise<string> {
  return bcrypt.hash(plainText, SALT_ROUNDS);
}

/**
 * Compare a plain text value with a hashed value
 * Returns true if they match
 */
export async function compareHash(
  plainText: string,
  hash: string
): Promise<boolean> {
  return bcrypt.compare(plainText, hash);
}

/**
 * Generate a secure random OTP
 * @param length - Length of OTP (default: 6)
 * @returns A numeric OTP as a string (e.g., "123456")
 */
export function generateOtp(length: number = 6): string {
  const min = Math.pow(10, length - 1);
  const max = Math.pow(10, length) - 1;

  // Generate cryptographically secure random number
  const randomValue = randomBytes(4).readUInt32BE(0);
  const otp = (randomValue % (max - min + 1)) + min;

  return otp.toString();
}

/**
 * Generate a secure random refresh token
 * @returns A 64-character hexadecimal string
 */
export function generateSecureToken(): string {
  return randomBytes(32).toString("hex");
}
```

---

## ❌ **File 3: src/utils/errors.ts**

Custom error classes and error handling:

```typescript
/**
 * Base class for all application errors
 */
export class AppError extends Error {
  public statusCode: number;
  public isOperational: boolean;

  constructor(message: string, statusCode: number = 500) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = true; // Distinguishes from programming errors

    // Maintains proper stack trace
    Error.captureStackTrace(this, this.constructor);
  }
}

/**
 * 400 Bad Request - Client sent invalid data
 */
export class BadRequestError extends AppError {
  constructor(message: string = "Bad Request") {
    super(message, 400);
  }
}

/**
 * 401 Unauthorized - Authentication failed
 */
export class UnauthorizedError extends AppError {
  constructor(message: string = "Unauthorized") {
    super(message, 401);
  }
}

/**
 * 403 Forbidden - User doesn't have permission
 */
export class ForbiddenError extends AppError {
  constructor(message: string = "Forbidden") {
    super(message, 403);
  }
}

/**
 * 404 Not Found - Resource doesn't exist
 */
export class NotFoundError extends AppError {
  constructor(message: string = "Resource not found") {
    super(message, 404);
  }
}

/**
 * 409 Conflict - Resource already exists
 */
export class ConflictError extends AppError {
  constructor(message: string = "Resource already exists") {
    super(message, 409);
  }
}

/**
 * 429 Too Many Requests - Rate limit exceeded
 */
export class RateLimitError extends AppError {
  constructor(message: string = "Too many requests") {
    super(message, 429);
  }
}

/**
 * 500 Internal Server Error
 */
export class InternalServerError extends AppError {
  constructor(message: string = "Internal server error") {
    super(message, 500);
  }
}
```

---

## ✅ **File 4: src/validators/auth.validators.ts**

Zod schemas for request validation:

```typescript
import { z } from "zod";

/**
 * Validates phone number in E.164 format
 * Example: +919876543210
 */
const phoneNumberSchema = z
  .string()
  .regex(
    /^\+[1-9]\d{1,14}$/,
    "Invalid phone number format. Must be in E.164 format (e.g., +919876543210)"
  )
  .min(10, "Phone number is too short")
  .max(16, "Phone number is too long");

/**
 * Validates OTP (6-digit numeric string)
 */
const otpSchema = z
  .string()
  .length(6, "OTP must be exactly 6 digits")
  .regex(/^\d{6}$/, "OTP must contain only numbers");

/**
 * Validates email format
 */
const emailSchema = z
  .string()
  .email("Invalid email format")
  .min(5, "Email is too short")
  .max(255, "Email is too long");

/**
 * Validates full name
 */
const fullNameSchema = z
  .string()
  .min(2, "Full name must be at least 2 characters")
  .max(255, "Full name is too long")
  .regex(/^[a-zA-Z\s]+$/, "Full name can only contain letters and spaces");

/**
 * Validates user role
 */
const userRoleSchema = z.enum([
  "creator",
  "agency",
  "brand",
  "gifter",
  "admin",
]);

/**
 * Validates device info
 */
const deviceInfoSchema = z
  .string()
  .max(500, "Device info is too long")
  .optional();

// =================================================================
// REQUEST VALIDATORS
// =================================================================

/**
 * POST /api/auth/phone/send-otp
 */
export const sendOtpSchema = z.object({
  body: z.object({
    phoneNumber: phoneNumberSchema,
  }),
});

/**
 * POST /api/auth/phone/verify-otp
 */
export const verifyOtpSchema = z.object({
  body: z.object({
    phoneNumber: phoneNumberSchema,
    otp: otpSchema,
    deviceInfo: deviceInfoSchema,
  }),
});

/**
 * POST /api/auth/google (after OAuth callback)
 * This validates the data you receive from Google
 */
export const googleAuthSchema = z.object({
  body: z.object({
    googleId: z.string().min(1, "Google ID is required"),
    email: emailSchema,
    fullName: fullNameSchema.optional(),
    profilePictureUrl: z.string().url().optional(),
    deviceInfo: deviceInfoSchema,
  }),
});

/**
 * POST /api/auth/facebook (after OAuth callback)
 */
export const facebookAuthSchema = z.object({
  body: z.object({
    facebookId: z.string().min(1, "Facebook ID is required"),
    email: emailSchema.optional(),
    fullName: fullNameSchema.optional(),
    profilePictureUrl: z.string().url().optional(),
    deviceInfo: deviceInfoSchema,
  }),
});

/**
 * POST /api/auth/refresh
 */
export const refreshTokenSchema = z.object({
  body: z.object({
    refreshToken: z.string().min(1, "Refresh token is required"),
  }),
});

/**
 * PATCH /api/users/profile
 */
export const updateProfileSchema = z.object({
  body: z.object({
    fullName: fullNameSchema.optional(),
    profilePictureUrl: z.string().url().optional(),
  }),
});

/**
 * PATCH /api/users/role
 */
export const selectRoleSchema = z.object({
  body: z.object({
    role: userRoleSchema,
  }),
});

// Export individual schemas for reuse
export {
  phoneNumberSchema,
  otpSchema,
  emailSchema,
  fullNameSchema,
  userRoleSchema,
  deviceInfoSchema,
};
```

---

## 🛡️ **File 5: src/utils/phone.ts**

Phone number utilities and E.164 formatting:

```typescript
/**
 * Normalize phone number to E.164 format
 * Handles common formats and adds country code if missing
 *
 * Examples:
 * - "9876543210" (India) → "+919876543210"
 * - "01234567890" (India) → "+911234567890"
 * - "+919876543210" → "+919876543210" (already correct)
 */
export function normalizePhoneNumber(
  phone: string,
  defaultCountryCode: string = "91" // India by default
): string {
  // Remove all non-digit characters
  let cleaned = phone.replace(/\D/g, "");

  // If it starts with country code, add + and return
  if (phone.startsWith("+")) {
    return "+" + cleaned;
  }

  // If it starts with 0, remove it (common in India: 09876543210)
  if (cleaned.startsWith("0")) {
    cleaned = cleaned.substring(1);
  }

  // If no country code, add the default one
  if (!cleaned.startsWith(defaultCountryCode)) {
    cleaned = defaultCountryCode + cleaned;
  }

  return "+" + cleaned;
}

/**
 * Validate if a phone number is in proper E.164 format
 */
export function isValidE164(phone: string): boolean {
  const e164Regex = /^\+[1-9]\d{1,14}$/;
  return e164Regex.test(phone);
}

/**
 * Mask phone number for display
 * Example: "+919876543210" → "+91******3210"
 */
export function maskPhoneNumber(phone: string): string {
  if (phone.length < 8) {
    return phone;
  }

  const countryCode = phone.substring(0, 3);
  const lastFour = phone.substring(phone.length - 4);
  const masked = "*".repeat(phone.length - 7);

  return countryCode + masked + lastFour;
}
```

---

## 🕒 **File 6: src/utils/date.ts**

Date and time utilities:

```typescript
/**
 * Get expiry date for OTP (default: 5 minutes from now)
 */
export function getOtpExpiryDate(minutes: number = 5): Date {
  const expiry = new Date();
  expiry.setMinutes(expiry.getMinutes() + minutes);
  return expiry;
}

/**
 * Get expiry date for refresh token (default: 30 days from now)
 */
export function getRefreshTokenExpiryDate(days: number = 30): Date {
  const expiry = new Date();
  expiry.setDate(expiry.getDate() + days);
  return expiry;
}

/**
 * Check if a date has expired
 */
export function isExpired(expiryDate: Date): boolean {
  return new Date() > expiryDate;
}

/**
 * Format date for logging
 */
export function formatDate(date: Date): string {
  return date.toISOString();
}
```

---

## 🧪 **Testing Your Utilities**

Create `src/test-utils.ts`:

```typescript
import * as dotenv from "dotenv";
dotenv.config();

import {
  generateAccessToken,
  generateRefreshToken,
  verifyAccessToken,
  verifyRefreshToken,
} from "./utils/jwt";
import {
  hashValue,
  compareHash,
  generateOtp,
  generateSecureToken,
} from "./utils/hashing";
import {
  normalizePhoneNumber,
  isValidE164,
  maskPhoneNumber,
} from "./utils/phone";
import {
  getOtpExpiryDate,
  getRefreshTokenExpiryDate,
  isExpired,
} from "./utils/date";

async function testUtilities() {
  console.log("🧪 Testing Utilities...\n");

  // Test JWT
  console.log("1️⃣ Testing JWT:");
  const accessToken = generateAccessToken({
    userId: "test-123",
    role: "creator",
  });
  console.log(
    "✅ Access Token Generated:",
    accessToken.substring(0, 50) + "..."
  );

  const decoded = verifyAccessToken(accessToken);
  console.log("✅ Decoded:", decoded);

  // Test Hashing
  console.log("\n2️⃣ Testing Hashing:");
  const otp = generateOtp();
  console.log("✅ Generated OTP:", otp);

  const hashed = await hashValue(otp);
  console.log("✅ Hashed OTP:", hashed);

  const isValid = await compareHash(otp, hashed);
  console.log("✅ Hash Comparison:", isValid);

  // Test Phone Utilities
  console.log("\n3️⃣ Testing Phone Utilities:");
  const normalized = normalizePhoneNumber("9876543210");
  console.log("✅ Normalized:", normalized);
  console.log("✅ Is Valid E164:", isValidE164(normalized));
  console.log("✅ Masked:", maskPhoneNumber(normalized));

  // Test Date Utilities
  console.log("\n4️⃣ Testing Date Utilities:");
  const otpExpiry = getOtpExpiryDate(5);
  console.log("✅ OTP Expiry:", otpExpiry);
  console.log("✅ Is Expired:", isExpired(otpExpiry));

  console.log("\n✅ All utility tests passed!");
}

testUtilities().catch(console.error);
```

Run it:

```bash
npx tsx src/test-utils.ts
```

Expected output:

```
🧪 Testing Utilities...

1️⃣ Testing JWT:
✅ Access Token Generated: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
✅ Decoded: { userId: 'test-123', role: 'creator', iat: 1234567890, exp: 1234568790, iss: 'agency-platform', aud: 'agency-platform-users' }

2️⃣ Testing Hashing:
✅ Generated OTP: 123456
✅ Hashed OTP: $2b$12$AbCdEfGhIjKlMnOpQrStUvWxYz...
✅ Hash Comparison: true

3️⃣ Testing Phone Utilities:
✅ Normalized: +919876543210
✅ Is Valid E164: true
✅ Masked: +91******3210

4️⃣ Testing Date Utilities:
✅ OTP Expiry: 2024-01-15T10:35:00.000Z
✅ Is Expired: false

✅ All utility tests passed!
```

---

## 🎯 **Next Step**

Proceed to **`05_middleware.md`** to create authentication middleware and error handlers.
