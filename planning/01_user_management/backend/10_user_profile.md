# Step 10: User Profile Management & Role Selection

## 👤 **Overview: Profile Management**

```
1. User logs in → role is NULL → must select role
2. User selects role → role is saved → can access protected routes
3. User can update profile (name, picture)
4. User can view their auth methods (phone, Google, Facebook)
```

---

## 🗄️ **File 1: src/services/user.service.ts**

User profile service:

```typescript
import { db } from "../db/connection";
import { users, userAuthIdentities } from "../db/schema";
import { eq } from "drizzle-orm";
import { NotFoundError, BadRequestError } from "../utils/errors";

/**
 * Get user profile by ID
 */
export async function getUserProfile(userId: string) {
  const userRecords = await db
    .select({
      id: users.id,
      role: users.role,
      fullName: users.fullName,
      profilePictureUrl: users.profilePictureUrl,
      createdAt: users.createdAt,
      updatedAt: users.updatedAt,
    })
    .from(users)
    .where(eq(users.id, userId))
    .limit(1);

  if (userRecords.length === 0) {
    throw new NotFoundError("User not found");
  }

  return userRecords[0];
}

/**
 * Get user with their auth identities
 */
export async function getUserWithAuthIdentities(userId: string) {
  const user = await getUserProfile(userId);

  const authIdentities = await db
    .select({
      provider: userAuthIdentities.provider,
      email: userAuthIdentities.email,
      createdAt: userAuthIdentities.createdAt,
    })
    .from(userAuthIdentities)
    .where(eq(userAuthIdentities.userId, userId));

  return {
    ...user,
    authMethods: authIdentities,
  };
}

/**
 * Update user profile
 */
export async function updateUserProfile(
  userId: string,
  updates: {
    fullName?: string;
    profilePictureUrl?: string;
  }
): Promise<any> {
  // Validate at least one field is being updated
  if (!updates.fullName && !updates.profilePictureUrl) {
    throw new BadRequestError("No fields to update");
  }

  const updatedUsers = await db
    .update(users)
    .set({
      ...updates,
      updatedAt: new Date(),
    })
    .where(eq(users.id, userId))
    .returning();

  if (updatedUsers.length === 0) {
    throw new NotFoundError("User not found");
  }

  return updatedUsers[0];
}

/**
 * Select user role (can only be done once or when NULL)
 */
export async function selectUserRole(
  userId: string,
  role: "creator" | "agency" | "brand" | "gifter"
): Promise<any> {
  // Check current role
  const currentUser = await getUserProfile(userId);

  if (currentUser.role !== null) {
    throw new BadRequestError(
      "Role has already been selected and cannot be changed"
    );
  }

  // Update role
  const updatedUsers = await db
    .update(users)
    .set({
      role,
      updatedAt: new Date(),
    })
    .where(eq(users.id, userId))
    .returning();

  return updatedUsers[0];
}

/**
 * Check if user has selected a role
 */
export async function hasSelectedRole(userId: string): Promise<boolean> {
  const user = await getUserProfile(userId);
  return user.role !== null;
}

/**
 * Delete user account (soft delete - removes user and cascades to auth identities and tokens)
 */
export async function deleteUserAccount(userId: string): Promise<void> {
  // This will cascade delete to:
  // - userAuthIdentities (onDelete: 'cascade')
  // - userRefreshTokens (onDelete: 'cascade')
  await db.delete(users).where(eq(users.id, userId));
}
```

---

## 🎯 **File 2: src/controllers/user.controller.ts**

User profile controllers:

```typescript
import { Response, NextFunction } from "express";
import { AuthRequest } from "../middleware/auth.middleware";
import {
  getUserProfile,
  getUserWithAuthIdentities,
  updateUserProfile,
  selectUserRole,
  deleteUserAccount,
} from "../services/user.service";
import { generateAccessToken } from "../utils/jwt";

/**
 * GET /api/users/me
 * Get current user profile
 */
export async function getProfile(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const user = await getUserProfile(req.user.userId);

    res.status(200).json({
      success: true,
      data: { user },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * GET /api/users/me/detailed
 * Get current user profile with auth methods
 */
export async function getDetailedProfile(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const user = await getUserWithAuthIdentities(req.user.userId);

    res.status(200).json({
      success: true,
      data: { user },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * PATCH /api/users/me
 * Update current user profile
 */
export async function updateProfile(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const { fullName, profilePictureUrl } = req.body;

    const updatedUser = await updateUserProfile(req.user.userId, {
      fullName,
      profilePictureUrl,
    });

    res.status(200).json({
      success: true,
      message: "Profile updated successfully",
      data: {
        user: {
          id: updatedUser.id,
          role: updatedUser.role,
          fullName: updatedUser.fullName,
          profilePictureUrl: updatedUser.profilePictureUrl,
        },
      },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * PATCH /api/users/me/role
 * Select user role (one-time action)
 */
export async function selectRole(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    const { role } = req.body;

    const updatedUser = await selectUserRole(req.user.userId, role);

    // Generate new access token with updated role
    const newAccessToken = generateAccessToken({
      userId: updatedUser.id,
      role: updatedUser.role,
    });

    res.status(200).json({
      success: true,
      message: "Role selected successfully",
      data: {
        user: {
          id: updatedUser.id,
          role: updatedUser.role,
          fullName: updatedUser.fullName,
          profilePictureUrl: updatedUser.profilePictureUrl,
        },
        // Send new access token with updated role
        accessToken: newAccessToken,
      },
    });
  } catch (error) {
    next(error);
  }
}

/**
 * DELETE /api/users/me
 * Delete user account
 */
export async function deleteAccount(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      return next(new Error("User not authenticated"));
    }

    await deleteUserAccount(req.user.userId);

    res.status(200).json({
      success: true,
      message: "Account deleted successfully",
    });
  } catch (error) {
    next(error);
  }
}
```

---

## 🛣️ **File 3: src/routes/user.routes.ts**

User profile routes:

```typescript
import { Router } from "express";
import {
  getProfile,
  getDetailedProfile,
  updateProfile,
  selectRole,
  deleteAccount,
} from "../controllers/user.controller";
import { authenticateToken } from "../middleware/auth.middleware";
import { validateRequest } from "../middleware/validation.middleware";
import {
  updateProfileSchema,
  selectRoleSchema,
} from "../validators/auth.validators";

const router = Router();

// All routes require authentication
router.use(authenticateToken);

/**
 * GET /api/users/me
 * Get current user profile
 */
router.get("/me", getProfile);

/**
 * GET /api/users/me/detailed
 * Get current user profile with auth methods
 */
router.get("/me/detailed", getDetailedProfile);

/**
 * PATCH /api/users/me
 * Update current user profile
 */
router.patch("/me", validateRequest(updateProfileSchema), updateProfile);

/**
 * PATCH /api/users/me/role
 * Select user role
 */
router.patch("/me/role", validateRequest(selectRoleSchema), selectRole);

/**
 * DELETE /api/users/me
 * Delete user account
 */
router.delete("/me", deleteAccount);

export default router;
```

---

## 🔗 **File 4: Update src/app.ts**

Add user routes:

```typescript
// ... existing imports ...

// Import user routes
import userRoutes from "./routes/user.routes";

// ... existing setup ...

// Add routes
app.use("/health", healthRoutes);
app.use("/api/auth/phone", phoneAuthRoutes);
app.use("/api/auth", oauthRoutes);
app.use("/api/auth", tokenRoutes);
app.use("/api/users", userRoutes); // ← Add this line

// ... rest of the file ...
```

---

## 🧪 **Testing User Profile**

### Test 1: Get Profile (Without Role)

Login first to get access token, then:

```bash
curl -X GET http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": null,
      "fullName": null,
      "profilePictureUrl": null,
      "createdAt": "2024-01-15T10:00:00.000Z",
      "updatedAt": "2024-01-15T10:00:00.000Z"
    }
  }
}
```

### Test 2: Select Role

```bash
curl -X PATCH http://localhost:5000/api/users/me/role \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "creator"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Role selected successfully",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": "creator",
      "fullName": null,
      "profilePictureUrl": null
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

⚠️ **Important**: Use the new `accessToken` from the response for subsequent requests!

### Test 3: Try to Change Role Again (Should Fail)

```bash
curl -X PATCH http://localhost:5000/api/users/me/role \
  -H "Authorization: Bearer YOUR_NEW_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "agency"
  }'
```

**Expected Response:**

```json
{
  "success": false,
  "message": "Role has already been selected and cannot be changed"
}
```

### Test 4: Update Profile

```bash
curl -X PATCH http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Ajay Kumar",
    "profilePictureUrl": "https://example.com/profile.jpg"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": "creator",
      "fullName": "Ajay Kumar",
      "profilePictureUrl": "https://example.com/profile.jpg"
    }
  }
}
```

### Test 5: Get Detailed Profile

```bash
curl -X GET http://localhost:5000/api/users/me/detailed \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "role": "creator",
      "fullName": "Ajay Kumar",
      "profilePictureUrl": "https://example.com/profile.jpg",
      "createdAt": "2024-01-15T10:00:00.000Z",
      "updatedAt": "2024-01-15T10:05:00.000Z",
      "authMethods": [
        {
          "provider": "phone",
          "email": null,
          "createdAt": "2024-01-15T10:00:00.000Z"
        }
      ]
    }
  }
}
```

### Test 6: Delete Account

```bash
curl -X DELETE http://localhost:5000/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

Now try to use the same access token - it should fail because the user no longer exists!

---

## 🎨 **Frontend Integration Notes**

### Role Selection Flow

1. **After Login/Registration**:

   ```javascript
   const response = await loginWithPhone(phone, otp);

   if (response.data.user.role === null) {
     // Redirect to role selection screen
     navigate("/select-role");
   } else {
     // Redirect to dashboard
     navigate("/dashboard");
   }
   ```

2. **Role Selection Screen**:

   - Show cards for: Creator, Agency, Brand, Gifter
   - After selection, update access token in storage
   - Redirect to dashboard

3. **Profile Update**:
   - Allow users to update name and profile picture anytime
   - Role cannot be changed once selected

---

## 🔒 **Security Considerations**

1. **Role Immutability**: Once a role is selected, it cannot be changed (prevents abuse)
2. **Profile Picture URLs**: Validate URLs to prevent XSS attacks
3. **Account Deletion**: Consider implementing soft delete with recovery period
4. **Audit Trail**: Consider logging profile changes for security
5. **Email Verification**: If user has email from OAuth, consider verifying before critical operations

---

## 📊 **Additional Features to Consider**

1. **Profile Completion Percentage**: Calculate based on filled fields
2. **Email/Phone Verification Badges**: Show verified status
3. **Account Age**: Display "Member since"
4. **Profile Visibility Settings**: For creator profiles
5. **Two-Factor Authentication**: Add extra security layer
6. **Activity Log**: Show recent logins and changes

---

## 🎯 **Next Step**

Proceed to **`11_testing_guide.md`** for comprehensive testing instructions and security checklist!
