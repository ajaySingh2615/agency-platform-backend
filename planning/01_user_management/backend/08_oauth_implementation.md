# Step 8: OAuth Implementation (Google & Facebook)

## 🔐 **Overview: OAuth Flow**

```
1. Frontend redirects to Google/Facebook → User grants permission
2. Provider redirects back with authorization code
3. Backend exchanges code for user info (email, name, picture)
4. Backend creates/finds user → Issues tokens
```

---

## 📦 **File 1: Install OAuth Dependencies**

Add these to your `package.json`:

```bash
npm install passport passport-google-oauth20 passport-facebook
npm install -D @types/passport @types/passport-google-oauth20 @types/passport-facebook
```

---

## 🔧 **File 2: src/config/passport.ts**

Configure Passport.js for OAuth:

```typescript
import passport from "passport";
import { Strategy as GoogleStrategy } from "passport-google-oauth20";
import { Strategy as FacebookStrategy } from "passport-facebook";

/**
 * Google OAuth Strategy
 */
if (process.env.GOOGLE_CLIENT_ID && process.env.GOOGLE_CLIENT_SECRET) {
  passport.use(
    new GoogleStrategy(
      {
        clientID: process.env.GOOGLE_CLIENT_ID,
        clientSecret: process.env.GOOGLE_CLIENT_SECRET,
        callbackURL:
          process.env.GOOGLE_CALLBACK_URL || "/api/auth/google/callback",
      },
      (accessToken, refreshToken, profile, done) => {
        // Extract user info from Google profile
        const userInfo = {
          googleId: profile.id,
          email: profile.emails?.[0]?.value || null,
          fullName: profile.displayName || null,
          profilePictureUrl: profile.photos?.[0]?.value || null,
        };

        return done(null, userInfo);
      }
    )
  );
}

/**
 * Facebook OAuth Strategy
 */
if (process.env.FACEBOOK_APP_ID && process.env.FACEBOOK_APP_SECRET) {
  passport.use(
    new FacebookStrategy(
      {
        clientID: process.env.FACEBOOK_APP_ID,
        clientSecret: process.env.FACEBOOK_APP_SECRET,
        callbackURL:
          process.env.FACEBOOK_CALLBACK_URL || "/api/auth/facebook/callback",
        profileFields: ["id", "displayName", "emails", "photos"],
      },
      (accessToken, refreshToken, profile, done) => {
        // Extract user info from Facebook profile
        const userInfo = {
          facebookId: profile.id,
          email: profile.emails?.[0]?.value || null,
          fullName: profile.displayName || null,
          profilePictureUrl: profile.photos?.[0]?.value || null,
        };

        return done(null, userInfo);
      }
    )
  );
}

export default passport;
```

---

## 🗄️ **File 3: src/services/oauth.service.ts**

OAuth authentication service:

```typescript
import { db } from "../db/connection";
import { users, userAuthIdentities } from "../db/schema";
import { eq, and } from "drizzle-orm";
import { createUserSession } from "./auth.service";
import { ConflictError } from "../utils/errors";

interface OAuthUserData {
  providerId: string;
  provider: "google" | "facebook";
  email: string | null;
  fullName: string | null;
  profilePictureUrl: string | null;
}

/**
 * Handle OAuth login/registration
 * Returns: { accessToken, refreshToken, user, isNewUser }
 */
export async function handleOAuthLogin(
  data: OAuthUserData,
  deviceInfo?: string
): Promise<{
  accessToken: string;
  refreshToken: string;
  user: any;
  isNewUser: boolean;
}> {
  // Check if auth identity exists
  const existingIdentity = await db
    .select()
    .from(userAuthIdentities)
    .where(
      and(
        eq(userAuthIdentities.provider, data.provider),
        eq(userAuthIdentities.providerId, data.providerId)
      )
    )
    .limit(1);

  let userId: string;
  let isNewUser = false;

  if (existingIdentity.length === 0) {
    // New user - create user and auth identity
    isNewUser = true;

    // Create user with pre-filled data from OAuth
    const newUsers = await db
      .insert(users)
      .values({
        role: null, // User must select role later
        fullName: data.fullName,
        profilePictureUrl: data.profilePictureUrl,
      })
      .returning();

    userId = newUsers[0].id;

    // Create auth identity
    await db.insert(userAuthIdentities).values({
      userId,
      provider: data.provider,
      providerId: data.providerId,
      email: data.email,
    });
  } else {
    // Existing user
    userId = existingIdentity[0].userId;

    // Optionally update user info if changed
    if (data.fullName || data.profilePictureUrl) {
      await db
        .update(users)
        .set({
          fullName: data.fullName,
          profilePictureUrl: data.profilePictureUrl,
          updatedAt: new Date(),
        })
        .where(eq(users.id, userId));
    }
  }

  // Get user details
  const userDetails = await db
    .select()
    .from(users)
    .where(eq(users.id, userId))
    .limit(1);

  const user = userDetails[0];

  // Create session (access + refresh tokens)
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
```

---

## 🎯 **File 4: src/controllers/oauth.controller.ts**

OAuth controllers:

```typescript
import { Request, Response, NextFunction } from "express";
import { handleOAuthLogin } from "../services/oauth.service";
import passport from "../config/passport";

/**
 * GET /api/auth/google
 * Initiate Google OAuth flow
 */
export function googleAuth(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  passport.authenticate("google", {
    scope: ["profile", "email"],
    session: false,
  })(req, res, next);
}

/**
 * GET /api/auth/google/callback
 * Handle Google OAuth callback
 */
export function googleCallback(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  passport.authenticate(
    "google",
    { session: false },
    async (err: any, user: any) => {
      if (err || !user) {
        // Redirect to frontend with error
        return res.redirect(
          `${
            process.env.FRONTEND_URL || "http://localhost:5173"
          }/auth/error?message=Google authentication failed`
        );
      }

      try {
        const deviceInfo = req.headers["user-agent"] || "Unknown device";

        const result = await handleOAuthLogin(
          {
            providerId: user.googleId,
            provider: "google",
            email: user.email,
            fullName: user.fullName,
            profilePictureUrl: user.profilePictureUrl,
          },
          deviceInfo
        );

        // Redirect to frontend with tokens (you might want to use a different approach)
        const frontendUrl = process.env.FRONTEND_URL || "http://localhost:5173";
        const params = new URLSearchParams({
          accessToken: result.accessToken,
          refreshToken: result.refreshToken,
          isNewUser: result.isNewUser.toString(),
        });

        res.redirect(`${frontendUrl}/auth/success?${params.toString()}`);
      } catch (error) {
        next(error);
      }
    }
  )(req, res, next);
}

/**
 * GET /api/auth/facebook
 * Initiate Facebook OAuth flow
 */
export function facebookAuth(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  passport.authenticate("facebook", {
    scope: ["email", "public_profile"],
    session: false,
  })(req, res, next);
}

/**
 * GET /api/auth/facebook/callback
 * Handle Facebook OAuth callback
 */
export function facebookCallback(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  passport.authenticate(
    "facebook",
    { session: false },
    async (err: any, user: any) => {
      if (err || !user) {
        return res.redirect(
          `${
            process.env.FRONTEND_URL || "http://localhost:5173"
          }/auth/error?message=Facebook authentication failed`
        );
      }

      try {
        const deviceInfo = req.headers["user-agent"] || "Unknown device";

        const result = await handleOAuthLogin(
          {
            providerId: user.facebookId,
            provider: "facebook",
            email: user.email,
            fullName: user.fullName,
            profilePictureUrl: user.profilePictureUrl,
          },
          deviceInfo
        );

        const frontendUrl = process.env.FRONTEND_URL || "http://localhost:5173";
        const params = new URLSearchParams({
          accessToken: result.accessToken,
          refreshToken: result.refreshToken,
          isNewUser: result.isNewUser.toString(),
        });

        res.redirect(`${frontendUrl}/auth/success?${params.toString()}`);
      } catch (error) {
        next(error);
      }
    }
  )(req, res, next);
}
```

---

## 🛣️ **File 5: src/routes/oauth.routes.ts**

OAuth routes:

```typescript
import { Router } from "express";
import {
  googleAuth,
  googleCallback,
  facebookAuth,
  facebookCallback,
} from "../controllers/oauth.controller";
import { authLimiter } from "../middleware/rateLimiter.middleware";

const router = Router();

/**
 * Google OAuth Routes
 */
router.get("/google", authLimiter, googleAuth);
router.get("/google/callback", googleCallback);

/**
 * Facebook OAuth Routes
 */
router.get("/facebook", authLimiter, facebookAuth);
router.get("/facebook/callback", facebookCallback);

export default router;
```

---

## 🔗 **File 6: Update src/app.ts**

Add OAuth routes and passport initialization:

```typescript
// ... existing imports ...

// Import passport and OAuth routes
import passport from "./config/passport";
import oauthRoutes from "./routes/oauth.routes";

// ... existing setup ...

// Initialize passport (add BEFORE routes)
app.use(passport.initialize());

// Add routes
app.use("/health", healthRoutes);
app.use("/api/auth/phone", phoneAuthRoutes);
app.use("/api/auth", oauthRoutes); // ← Add this line

// ... rest of the file ...
```

---

## 🔧 **File 7: Update .env**

Add frontend URL:

```env
FRONTEND_URL=http://localhost:5173
```

---

## 🧪 **Testing OAuth (Development)**

### Important: OAuth Testing Requirements

1. **HTTPS Required for Production**: OAuth providers require HTTPS for callbacks
2. **For Local Development**: Use tools like ngrok or configure localhost in provider console

### Setting Up Google OAuth

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable "Google+ API"
4. Create OAuth credentials:
   - Application type: Web application
   - Authorized JavaScript origins: `http://localhost:5000`
   - Authorized redirect URIs: `http://localhost:5000/api/auth/google/callback`
5. Copy Client ID and Client Secret to your `.env`

### Setting Up Facebook OAuth

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add "Facebook Login" product
4. Configure OAuth redirect URIs: `http://localhost:5000/api/auth/facebook/callback`
5. Copy App ID and App Secret to your `.env`

### Test Google OAuth

1. Start your server:

```bash
npm run dev
```

2. Open in browser:

```
http://localhost:5000/api/auth/google
```

3. You'll be redirected to Google login
4. After granting permission, you'll be redirected back with tokens

### Expected Flow

```
Browser → http://localhost:5000/api/auth/google
       → Google Login Page
       → User grants permission
       → http://localhost:5000/api/auth/google/callback
       → http://localhost:5173/auth/success?accessToken=...&refreshToken=...&isNewUser=true
```

---

## 🎨 **Alternative: API-Based OAuth (Recommended for SPAs)**

Instead of server-side redirects, you can use a more modern approach:

### File: src/controllers/oauth-api.controller.ts

```typescript
import { Request, Response, NextFunction } from "express";
import { handleOAuthLogin } from "../services/oauth.service";
import { validateRequest } from "../middleware/validation.middleware";
import {
  googleAuthSchema,
  facebookAuthSchema,
} from "../validators/auth.validators";

/**
 * POST /api/auth/google/verify
 * Verify Google ID token from frontend
 */
export async function verifyGoogleToken(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { googleId, email, fullName, profilePictureUrl, deviceInfo } =
      req.body;

    const result = await handleOAuthLogin(
      {
        providerId: googleId,
        provider: "google",
        email,
        fullName,
        profilePictureUrl,
      },
      deviceInfo
    );

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

/**
 * POST /api/auth/facebook/verify
 * Verify Facebook access token from frontend
 */
export async function verifyFacebookToken(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    const { facebookId, email, fullName, profilePictureUrl, deviceInfo } =
      req.body;

    const result = await handleOAuthLogin(
      {
        providerId: facebookId,
        provider: "facebook",
        email,
        fullName,
        profilePictureUrl,
      },
      deviceInfo
    );

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

This approach lets your frontend handle OAuth with Google/Facebook SDKs, then send the verified user data to your backend.

---

## 🎯 **Next Step**

Proceed to **`09_token_management.md`** to implement token refresh and logout functionality!
