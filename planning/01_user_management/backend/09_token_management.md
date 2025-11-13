# Step 9: Token Management - Refresh & Logout

## 🔄 **Overview: Token Refresh Flow**

```
1. Access token expires (15 minutes)
2. Frontend sends refresh token to /api/auth/refresh
3. Backend validates refresh token → Issues new access token
4. Frontend uses new access token for API calls
```

---

## 🗄️ **File 1: src/services/token.service.ts**

Token management service:

```typescript
import { db } from "../db/connection";
import { userRefreshTokens, users } from "../db/schema";
import { eq } from "drizzle-orm";
import { verifyRefreshToken, generateAccessToken } from "../utils/jwt";
import { compareHash } from "../utils/hashing";
import { isExpired } from "../utils/date";
import { UnauthorizedError, NotFoundError } from "../utils/errors";

/**
 * Refresh access token using refresh token
 * Returns: new access token
 */
export async function refreshAccessToken(
  refreshToken: string
): Promise<string> {
  // Verify JWT refresh token
  let decoded;
  try {
    decoded = verifyRefreshToken(refreshToken);
  } catch (error) {
    throw new UnauthorizedError("Invalid or expired refresh token");
  }

  const { userId, tokenId } = decoded;

  // Find the refresh token in database
  const tokenRecords = await db
    .select()
    .from(userRefreshTokens)
    .where(eq(userRefreshTokens.id, tokenId))
    .limit(1);

  if (tokenRecords.length === 0) {
    throw new UnauthorizedError("Refresh token not found. Please login again.");
  }

  const tokenRecord = tokenRecords[0];

  // Check if token belongs to the claimed user
  if (tokenRecord.userId !== userId) {
    throw new UnauthorizedError("Token mismatch");
  }

  // Check if token has expired
  if (isExpired(tokenRecord.expiresAt)) {
    // Delete expired token
    await db.delete(userRefreshTokens).where(eq(userRefreshTokens.id, tokenId));

    throw new UnauthorizedError(
      "Refresh token has expired. Please login again."
    );
  }

  // Get user details for new access token
  const userRecords = await db
    .select()
    .from(users)
    .where(eq(users.id, userId))
    .limit(1);

  if (userRecords.length === 0) {
    throw new NotFoundError("User not found");
  }

  const user = userRecords[0];

  // Generate new access token
  const newAccessToken = generateAccessToken({
    userId: user.id,
    role: user.role,
  });

  return newAccessToken;
}

/**
 * Logout user (delete specific refresh token)
 */
export async function logoutUser(refreshToken: string): Promise<void> {
  // Verify JWT refresh token
  let decoded;
  try {
    decoded = verifyRefreshToken(refreshToken);
  } catch (error) {
    // Even if token is invalid/expired, try to delete it
    // This is a best-effort cleanup
    return;
  }

  const { tokenId } = decoded;

  // Delete the refresh token
  await db.delete(userRefreshTokens).where(eq(userRefreshTokens.id, tokenId));
}

/**
 * Logout from all devices (delete all refresh tokens for user)
 */
export async function logoutAllDevices(userId: string): Promise<void> {
  await db
    .delete(userRefreshTokens)
    .where(eq(userRefreshTokens.userId, userId));
}

/**
 * Get all active sessions for a user
 */
export async function getUserSessions(userId: string): Promise<any[]> {
  const sessions = await db
    .select({
      id: userRefreshTokens.id,
      deviceInfo: userRefreshTokens.deviceInfo,
      createdAt: userRefreshTokens.createdAt,
      expiresAt: userRefreshTokens.expiresAt,
    })
    .from(userRefreshTokens)
    .where(eq(userRefreshTokens.userId, userId));

  return sessions;
}

/**
 * Revoke a specific session
 */
export async function revokeSession(
  userId: string,
  sessionId: string
): Promise<void> {
  // Verify the session belongs to the user before deleting
  const session = await db
    .select()
    .from(userRefreshTokens)
    .where(eq(userRefreshTokens.id, sessionId))
    .limit(1);

  if (session.length === 0) {
    throw new NotFoundError("Session not found");
  }

  if (session[0].userId !== userId) {
    throw new UnauthorizedError("Cannot revoke another user's session");
  }

  await db.delete(userRefreshTokens).where(eq(userRefreshTokens.id, sessionId));
}
```

---

## 🎯 **File 2: src/controllers/token.controller.ts**

Token management controllers:

```typescript
import { Response, NextFunction } from "express";
import { AuthRequest } from "../middleware/auth.middleware";
import {
  refreshAccessToken,
  logoutUser,
  logoutAllDevices,
  getUserSessions,
  revokeSession,
} from "../services/token.service";

/**
 * POST /api/auth/refresh
 * Refresh access token
 */
export async function refresh(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { refreshToken } = req.body;

    const newAccessToken = await refreshAccessToken(refreshToken);

    res.status(200).json({
      success: true,
      message: "Access token refreshed successfully",
      data: {
        accessToken: newAccessToken,
      },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * POST /api/auth/logout
 * Logout from current device
 */
export async function logout(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { refreshToken } = req.body;

    await logoutUser(refreshToken);

    res.status(200).json({
      success: true,
      message: "Logged out successfully",
    });
  } catch (error) {
    next(error);
  }
}

/**
 * POST /api/auth/logout-all
 * Logout from all devices
 * Requires authentication
 */
export async function logoutAll(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    await logoutAllDevices(req.user.userId);

    res.status(200).json({
      success: true,
      message: "Logged out from all devices successfully",
    });
  } catch (error) {
    next(error);
  }
}

/**
 * GET /api/auth/sessions
 * Get all active sessions
 * Requires authentication
 */
export async function getSessions(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const sessions = await getUserSessions(req.user.userId);

    res.status(200).json({
      success: true,
      data: {
        sessions,
        count: sessions.length,
      },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * DELETE /api/auth/sessions/:sessionId
 * Revoke a specific session
 * Requires authentication
 */
export async function deleteSession(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const { sessionId } = req.params;

    await revokeSession(req.user.userId, sessionId);

    res.status(200).json({
      success: true,
      message: "Session revoked successfully",
    });
  } catch (error) {
    next(error);
  }
}
```

---

## 🛣️ **File 3: src/routes/token.routes.ts**

Token management routes:

```typescript
import { Router } from "express";
import {
  refresh,
  logout,
  logoutAll,
  getSessions,
  deleteSession,
} from "../controllers/token.controller";
import { validateRequest } from "../middleware/validation.middleware";
import { refreshTokenSchema } from "../validators/auth.validators";
import { authenticateToken } from "../middleware/auth.middleware";
import { refreshLimiter } from "../middleware/rateLimiter.middleware";

const router = Router();

/**
 * POST /api/auth/refresh
 * Refresh access token
 */
router.post(
  "/refresh",
  refreshLimiter,
  validateRequest(refreshTokenSchema),
  refresh
);

/**
 * POST /api/auth/logout
 * Logout from current device
 */
router.post("/logout", validateRequest(refreshTokenSchema), logout);

/**
 * POST /api/auth/logout-all
 * Logout from all devices (requires authentication)
 */
router.post("/logout-all", authenticateToken, logoutAll);

/**
 * GET /api/auth/sessions
 * Get all active sessions (requires authentication)
 */
router.get("/sessions", authenticateToken, getSessions);

/**
 * DELETE /api/auth/sessions/:sessionId
 * Revoke a specific session (requires authentication)
 */
router.delete("/sessions/:sessionId", authenticateToken, deleteSession);

export default router;
```

---

## 🔗 **File 4: Update src/app.ts**

Add token management routes:

```typescript
// ... existing imports ...

// Import token routes
import tokenRoutes from "./routes/token.routes";

// ... existing setup ...

// Add routes
app.use("/health", healthRoutes);
app.use("/api/auth/phone", phoneAuthRoutes);
app.use("/api/auth", oauthRoutes);
app.use("/api/auth", tokenRoutes); // ← Add this line

// ... rest of the file ...
```

---

## 🧪 **Testing Token Management**

### Test 1: Refresh Access Token

First, login to get tokens:

```bash
# Send OTP
curl -X POST http://localhost:5000/api/auth/phone/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber": "+919876543210"}'

# Verify OTP (check console for OTP)
curl -X POST http://localhost:5000/api/auth/phone/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "123456",
    "deviceInfo": "Chrome on Windows"
  }'
```

Save the `refreshToken` from the response, then:

```bash
curl -X POST http://localhost:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Access token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Test 2: Get Active Sessions

```bash
curl -X GET http://localhost:5000/api/auth/sessions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "sessions": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "deviceInfo": "Chrome on Windows",
        "createdAt": "2024-01-15T10:00:00.000Z",
        "expiresAt": "2024-02-14T10:00:00.000Z"
      }
    ],
    "count": 1
  }
}
```

### Test 3: Logout from Current Device

```bash
curl -X POST http://localhost:5000/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

Now try to refresh with the same token - it should fail:

```bash
curl -X POST http://localhost:5000/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Refresh token not found. Please login again."
}
```

### Test 4: Logout from All Devices

Login from 2 different "devices" (send different deviceInfo), then:

```bash
curl -X POST http://localhost:5000/api/auth/logout-all \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Logged out from all devices successfully"
}
```

All refresh tokens should now be invalid!

### Test 5: Revoke Specific Session

Get session ID from `/sessions` endpoint, then:

```bash
curl -X DELETE http://localhost:5000/api/auth/sessions/SESSION_ID_HERE \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Session revoked successfully"
}
```

---

## 🔐 **Security Notes**

1. **Refresh Token Rotation**: Consider implementing refresh token rotation (issue new refresh token on each refresh)
2. **Token Blacklisting**: Current implementation uses database-stored tokens (stateful), which allows instant revocation
3. **Suspicious Activity**: Monitor rapid token refresh patterns (could indicate stolen tokens)
4. **HTTPS Only**: In production, only send tokens over HTTPS
5. **httpOnly Cookies**: Consider storing refresh tokens in httpOnly cookies instead of localStorage

---

## 📊 **Session Management Best Practices**

1. **Display Active Sessions**: Show users their active devices/locations
2. **One-Click Logout**: Allow users to revoke suspicious sessions
3. **Automatic Cleanup**: Run periodic cleanup job to delete expired tokens
4. **Email Notifications**: Notify users when new device logs in
5. **Suspicious Activity Detection**: Flag logins from new locations/devices

---

## 🎯 **Next Step**

Proceed to **`10_user_profile.md`** to implement user profile management and role selection!
