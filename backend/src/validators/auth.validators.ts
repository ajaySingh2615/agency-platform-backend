import { z } from "zod";

// Validates phone number in E.164 format
// Example: +919876543210
const phoneNumberSchema = z
  .string()
  .regex(
    /^\+[1-9]\d{1,14}$/,
    "Invalid phone number format. Must be in E.164 format (e.g., +919876543210)"
  )
  .min(10, "Phone number is too short")
  .max(16, "Phone number is too long");

const otpSchema = z
  .string()
  .length(6, "OTP must be exactly 6 digits")
  .regex(/^\d{6}$/, "OTP must contain only numbers");

const emailSchema = z
  .string()
  .email("Invalid email format")
  .min(5, "Email is too short")
  .max(255, "Email is too long");

const fullNameSchema = z
  .string()
  .min(2, "Full name must be at least 2 characters")
  .max(255, "Full name is too long")
  .regex(/^[a-zA-Z\s]+$/, "Full name can only contain letters and spaces");

const userRoleSchema = z.enum([
  "creator",
  "agency",
  "brand",
  "gifter",
  "admin",
]);

const deviceInfoSchema = z
  .string()
  .max(500, "Device info is too long")
  .optional();

// =================================================================
// REQUEST VALIDATORS
// =================================================================

// POST /api/auth/phone/send-otp
export const sendOtpSchema = z.object({
  body: z.object({
    phoneNumber: phoneNumberSchema,
  }),
});

// POST /api/auth/phone/verify-otp
export const verifyOtpSchema = z.object({
  body: z.object({
    phoneNumber: phoneNumberSchema,
    otp: otpSchema,
    deviceInfo: deviceInfoSchema,
  }),
});

// POST /api/auth/google (after OAuth callback)
// This validates the data you receive from Google
export const googleAuthSchema = z.object({
  body: z.object({
    googleId: z.string().min(1, "Google ID is required"),
    email: emailSchema,
    fullName: fullNameSchema.optional(),
    profilePictureUrl: z.string().url().optional(),
    deviceInfo: deviceInfoSchema,
  }),
});

// POST /api/auth/facebook (after OAuth callback)
export const facebookAuthSchema = z.object({
  body: z.object({
    facebookId: z.string().min(1, "Facebook ID is required"),
    email: emailSchema.optional(),
    fullName: fullNameSchema.optional(),
    profilePictureUrl: z.string().url().optional(),
    deviceInfo: deviceInfoSchema,
  }),
});

// POST /api/auth/refresh
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
