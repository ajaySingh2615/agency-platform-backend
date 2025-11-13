# Step 7: Phone Authentication Implementation

## 📱 **Overview: Phone OTP Flow**

```
1. User enters phone number → Backend generates OTP → SMS sent → OTP hash stored
2. User enters OTP → Backend verifies → Creates/finds user → Issues tokens
```

---

## 🔌 **File 1: src/services/sms.service.ts**

SMS sending service (abstracted for easy provider switching):

```typescript
/**
 * SMS Service
 * Currently using console.log for development
 * In production, replace with Twilio, AWS SNS, or another provider
 */

interface SmsProvider {
  sendSms(phoneNumber: string, message: string): Promise<boolean>;
}

/**
 * Console SMS Provider (for development/testing)
 */
class ConsoleSmsProvider implements SmsProvider {
  async sendSms(phoneNumber: string, message: string): Promise<boolean> {
    console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    console.log("📱 SMS SENT (Console Mode)");
    console.log(`To: ${phoneNumber}`);
    console.log(`Message: ${message}`);
    console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    return true;
  }
}

/**
 * Twilio SMS Provider (for production)
 * Uncomment and configure when ready to use
 */
/*
import twilio from 'twilio';

class TwilioSmsProvider implements SmsProvider {
  private client;

  constructor() {
    const accountSid = process.env.TWILIO_ACCOUNT_SID;
    const authToken = process.env.TWILIO_AUTH_TOKEN;

    if (!accountSid || !authToken) {
      throw new Error('Twilio credentials not configured');
    }

    this.client = twilio(accountSid, authToken);
  }

  async sendSms(phoneNumber: string, message: string): Promise<boolean> {
    try {
      await this.client.messages.create({
        body: message,
        from: process.env.TWILIO_PHONE_NUMBER,
        to: phoneNumber,
      });
      return true;
    } catch (error) {
      console.error('Twilio SMS error:', error);
      return false;
    }
  }
}
*/

/**
 * SMS Service - Singleton
 */
class SmsService {
  private provider: SmsProvider;

  constructor() {
    // Switch provider based on environment
    if (process.env.NODE_ENV === "production") {
      // Use TwilioSmsProvider in production
      // this.provider = new TwilioSmsProvider();
      this.provider = new ConsoleSmsProvider(); // Replace with Twilio
    } else {
      this.provider = new ConsoleSmsProvider();
    }
  }

  async sendOtp(phoneNumber: string, otp: string): Promise<boolean> {
    const message = `Your verification code is: ${otp}. Valid for 5 minutes. Do not share this code.`;
    return this.provider.sendSms(phoneNumber, message);
  }
}

export default new SmsService();
```

---

## 🗄️ **File 2: src/services/auth.service.ts**

Core authentication service with database operations:

```typescript
import { db } from "../db/connection";
import {
  users,
  userAuthIdentities,
  userRefreshTokens,
  phoneOtps,
} from "../db/schema";
import { eq, and } from "drizzle-orm";
import {
  hashValue,
  compareHash,
  generateOtp,
  generateSecureToken,
} from "../utils/hashing";
import {
  getOtpExpiryDate,
  getRefreshTokenExpiryDate,
  isExpired,
} from "../utils/date";
import { normalizePhoneNumber } from "../utils/phone";
import smsService from "./sms.service";
import {
  BadRequestError,
  UnauthorizedError,
  ConflictError,
  NotFoundError,
} from "../utils/errors";

/**
 * Send OTP to phone number
 */
export async function sendPhoneOtp(phoneNumber: string): Promise<void> {
  // Normalize phone number
  const normalizedPhone = normalizePhoneNumber(phoneNumber);

  // Generate OTP
  const otp = generateOtp(6);
  const otpHash = await hashValue(otp);
  const expiresAt = getOtpExpiryDate(5); // 5 minutes

  // Delete any existing OTP for this phone number
  await db.delete(phoneOtps).where(eq(phoneOtps.phoneNumber, normalizedPhone));

  // Store new OTP
  await db.insert(phoneOtps).values({
    phoneNumber: normalizedPhone,
    otpHash,
    expiresAt,
  });

  // Send OTP via SMS
  const sent = await smsService.sendOtp(normalizedPhone, otp);

  if (!sent) {
    throw new BadRequestError("Failed to send OTP. Please try again.");
  }
}

/**
 * Verify OTP and create/login user
 * Returns: { accessToken, refreshToken, user, isNewUser }
 */
export async function verifyPhoneOtp(
  phoneNumber: string,
  otp: string,
  deviceInfo?: string
): Promise<{
  accessToken: string;
  refreshToken: string;
  user: any;
  isNewUser: boolean;
}> {
  const normalizedPhone = normalizePhoneNumber(phoneNumber);

  // Find OTP record
  const otpRecords = await db
    .select()
    .from(phoneOtps)
    .where(eq(phoneOtps.phoneNumber, normalizedPhone))
    .limit(1);

  if (otpRecords.length === 0) {
    throw new UnauthorizedError("Invalid or expired OTP");
  }

  const otpRecord = otpRecords[0];

  // Check if OTP is expired
  if (isExpired(otpRecord.expiresAt)) {
    await db.delete(phoneOtps).where(eq(phoneOtps.id, otpRecord.id));
    throw new UnauthorizedError("OTP has expired. Please request a new one.");
  }

  // Verify OTP
  const isValid = await compareHash(otp, otpRecord.otpHash);

  if (!isValid) {
    throw new UnauthorizedError("Invalid OTP");
  }

  // Delete used OTP
  await db.delete(phoneOtps).where(eq(phoneOtps.id, otpRecord.id));

  // Check if user exists with this phone number
  const existingIdentity = await db
    .select()
    .from(userAuthIdentities)
    .where(
      and(
        eq(userAuthIdentities.provider, "phone"),
        eq(userAuthIdentities.providerId, normalizedPhone)
      )
    )
    .limit(1);

  let userId: string;
  let isNewUser = false;

  if (existingIdentity.length === 0) {
    // New user - create user and auth identity
    isNewUser = true;

    // Create user
    const newUsers = await db
      .insert(users)
      .values({
        role: null, // User must select role later
        fullName: null,
        profilePictureUrl: null,
      })
      .returning();

    userId = newUsers[0].id;

    // Create auth identity
    await db.insert(userAuthIdentities).values({
      userId,
      provider: "phone",
      providerId: normalizedPhone,
      email: null,
    });
  } else {
    // Existing user
    userId = existingIdentity[0].userId;
  }

  // Get user details
  const userDetails = await db
    .select()
    .from(users)
    .where(eq(users.id, userId))
    .limit(1);

  const user = userDetails[0];

  // Create tokens
  const { accessToken, refreshToken } = await createUserSession(
    userId,
    user.role,
    deviceInfo
  );

  return {
    accessToken,
    refreshToken,
    user,
    isNewUser,
  };
}

/**
 * Create a new session for user (handles device limit)
 */
export async function createUserSession(
  userId: string,
  role: string | null,
  deviceInfo?: string
): Promise<{ accessToken: string; refreshToken: string }> {
  const MAX_SESSIONS = parseInt(process.env.MAX_DEVICE_SESSIONS || "2");

  // Count existing active sessions
  const activeSessions = await db
    .select()
    .from(userRefreshTokens)
    .where(eq(userRefreshTokens.userId, userId));

  // If at limit, delete the oldest session
  if (activeSessions.length >= MAX_SESSIONS) {
    const oldestSession = activeSessions.sort(
      (a, b) => a.createdAt.getTime() - b.createdAt.getTime()
    )[0];

    await db
      .delete(userRefreshTokens)
      .where(eq(userRefreshTokens.id, oldestSession.id));

    console.log(
      `🔄 Removed oldest session for user ${userId} due to device limit`
    );
  }

  // Generate refresh token
  const refreshTokenPlain = generateSecureToken();
  const refreshTokenHash = await hashValue(refreshTokenPlain);
  const expiresAt = getRefreshTokenExpiryDate(30); // 30 days

  // Store refresh token
  const newTokens = await db
    .insert(userRefreshTokens)
    .values({
      userId,
      tokenHash: refreshTokenHash,
      deviceInfo: deviceInfo || null,
      expiresAt,
    })
    .returning();

  const tokenId = newTokens[0].id;

  // Generate access token
  const { generateAccessToken, generateRefreshToken } = await import(
    "../utils/jwt"
  );

  const accessToken = generateAccessToken({ userId, role });
  const refreshToken = generateRefreshToken({ userId, tokenId });

  return { accessToken, refreshToken };
}
```

---

## 🎯 **File 3: src/controllers/phone-auth.controller.ts**

Controller to handle HTTP requests:

```typescript
import { Response, NextFunction } from "express";
import { AuthRequest } from "../middleware/auth.middleware";
import { sendPhoneOtp, verifyPhoneOtp } from "../services/auth.service";
import { normalizePhoneNumber } from "../utils/phone";

/**
 * POST /api/auth/phone/send-otp
 * Send OTP to phone number
 */
export async function sendOtp(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { phoneNumber } = req.body;

    await sendPhoneOtp(phoneNumber);

    res.status(200).json({
      success: true,
      message: "OTP sent successfully",
      phoneNumber: normalizePhoneNumber(phoneNumber),
    });
  } catch (error) {
    next(error);
  }
}

/**
 * POST /api/auth/phone/verify-otp
 * Verify OTP and login/register user
 */
export async function verifyOtp(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { phoneNumber, otp, deviceInfo } = req.body;

    const result = await verifyPhoneOtp(phoneNumber, otp, deviceInfo);

    res.status(200).json({
      success: true,
      message: result.isNewUser
        ? "Account created successfully"
        : "Login successful",
      data: {
        accessToken: result.accessToken,
        refreshToken: result.refreshToken,
        user: {
          id: result.user.id,
          role: result.user.role,
          fullName: result.user.fullName,
          profilePictureUrl: result.user.profilePictureUrl,
        },
        isNewUser: result.isNewUser,
      },
    });
  } catch (error) {
    next(error);
  }
}
```

---

## 🛣️ **File 4: src/routes/phone-auth.routes.ts**

Define phone authentication routes:

```typescript
import { Router } from "express";
import { sendOtp, verifyOtp } from "../controllers/phone-auth.controller";
import { validateRequest } from "../middleware/validation.middleware";
import { sendOtpSchema, verifyOtpSchema } from "../validators/auth.validators";
import {
  otpSendLimiter,
  otpVerifyLimiter,
} from "../middleware/rateLimiter.middleware";

const router = Router();

/**
 * POST /api/auth/phone/send-otp
 * Send OTP to phone number
 */
router.post(
  "/send-otp",
  otpSendLimiter, // 3 requests per hour
  validateRequest(sendOtpSchema),
  sendOtp
);

/**
 * POST /api/auth/phone/verify-otp
 * Verify OTP and login/register
 */
router.post(
  "/verify-otp",
  otpVerifyLimiter, // 5 attempts per 15 minutes
  validateRequest(verifyOtpSchema),
  verifyOtp
);

export default router;
```

---

## 🔗 **File 5: Update src/app.ts**

Add phone auth routes to your Express app:

```typescript
// ... existing imports ...

// Import phone auth routes
import phoneAuthRoutes from "./routes/phone-auth.routes";

// ... existing setup ...

// Add routes
app.use("/health", healthRoutes);
app.use("/api/auth/phone", phoneAuthRoutes); // ← Add this line

// ... rest of the file ...
```

---

## 🧪 **Testing Phone Authentication**

### Test 1: Send OTP

```bash
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "OTP sent successfully",
  "phoneNumber": "+919876543210"
}
```

**Console Output:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📱 SMS SENT (Console Mode)
To: +919876543210
Message: Your verification code is: 123456. Valid for 5 minutes. Do not share this code.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Test 2: Verify OTP (Copy OTP from console)

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "123456",
    "deviceInfo": "Chrome on Windows"
  }'
```

**Expected Response (New User):**

```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "a1b2c3d4e5f6...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": null,
      "fullName": null,
      "profilePictureUrl": null
    },
    "isNewUser": true
  }
}
```

### Test 3: Verify Same User Again

Send OTP again, verify with new OTP:

**Expected Response (Existing User):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "g7h8i9j0k1l2...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": null,
      "fullName": null,
      "profilePictureUrl": null
    },
    "isNewUser": false
  }
}
```

### Test 4: Rate Limiting

Try sending OTP 4 times in a row:

```bash
# Request 1, 2, 3 will succeed
# Request 4 will fail with:
```

```json
{
  "success": false,
  "message": "Too many OTP requests, please try again after an hour"
}
```

### Test 5: Invalid OTP

```bash
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "000000",
    "deviceInfo": "Chrome on Windows"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Invalid OTP"
}
```

---

## 📊 **Database Verification**

Check what got created in the database:

```bash
npm run db:studio
```

Then open `https://local.drizzle.studio` and verify:

1. **users** table has a new row with `role: null`
2. **userAuthIdentities** table has a row with `provider: 'phone'` and `providerId: '+919876543210'`
3. **userRefreshTokens** table has a token hash stored
4. **phoneOtps** table is empty (OTP deleted after verification)

---

## 🎯 **Next Step**

Proceed to **`08_oauth_implementation.md`** to add Google and Facebook authentication!
