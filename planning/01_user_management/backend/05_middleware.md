# Step 5: Middleware - Authentication, Validation & Error Handling

## 🛡️ **File 1: src/middleware/auth.middleware.ts**

Authentication middleware to protect routes:

```typescript
import { Request, Response, NextFunction } from "express";
import { verifyAccessToken, extractBearerToken } from "../utils/jwt";
import { UnauthorizedError, ForbiddenError } from "../utils/errors";

/**
 * Extended Express Request with user information
 */
export interface AuthRequest extends Request {
  user?: {
    userId: string;
    role: string | null;
  };
}

/**
 * Middleware to verify JWT access token
 * Adds user info to request object if valid
 */
export function authenticateToken(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): void {
  try {
    const authHeader = req.headers.authorization;
    const token = extractBearerToken(authHeader);

    if (!token) {
      throw new UnauthorizedError("Access token is required");
    }

    const decoded = verifyAccessToken(token);

    // Attach user info to request
    req.user = {
      userId: decoded.userId,
      role: decoded.role,
    };

    next();
  } catch (error) {
    next(error);
  }
}

/**
 * Middleware to check if user has selected a role
 * Must be used AFTER authenticateToken
 */
export function requireRole(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): void {
  try {
    if (!req.user) {
      throw new UnauthorizedError("Authentication required");
    }

    if (!req.user.role) {
      throw new ForbiddenError(
        "Please select a role before accessing this resource"
      );
    }

    next();
  } catch (error) {
    next(error);
  }
}

/**
 * Middleware to check if user has one of the allowed roles
 * Must be used AFTER authenticateToken
 */
export function requireRoles(allowedRoles: string[]) {
  return (req: AuthRequest, res: Response, next: NextFunction): void => {
    try {
      if (!req.user) {
        throw new UnauthorizedError("Authentication required");
      }

      if (!req.user.role) {
        throw new ForbiddenError("Role selection required");
      }

      if (!allowedRoles.includes(req.user.role)) {
        throw new ForbiddenError(
          "You do not have permission to access this resource"
        );
      }

      next();
    } catch (error) {
      next(error);
    }
  };
}

/**
 * Optional authentication - adds user to request if token exists, but doesn't fail if missing
 */
export function optionalAuth(
  req: AuthRequest,
  res: Response,
  next: NextFunction
): void {
  try {
    const authHeader = req.headers.authorization;
    const token = extractBearerToken(authHeader);

    if (token) {
      const decoded = verifyAccessToken(token);
      req.user = {
        userId: decoded.userId,
        role: decoded.role,
      };
    }

    next();
  } catch (error) {
    // If token is invalid, just continue without user
    next();
  }
}
```

---

## ✅ **File 2: src/middleware/validation.middleware.ts**

Request validation middleware using Zod:

```typescript
import { Request, Response, NextFunction } from "express";
import { AnyZodObject, ZodError } from "zod";
import { BadRequestError } from "../utils/errors";

/**
 * Middleware to validate request using Zod schema
 */
export function validateRequest(schema: AnyZodObject) {
  return async (
    req: Request,
    res: Response,
    next: NextFunction
  ): Promise<void> => {
    try {
      await schema.parseAsync({
        body: req.body,
        query: req.query,
        params: req.params,
      });
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        const errorMessages = error.errors.map((err) => ({
          field: err.path.join("."),
          message: err.message,
        }));

        next(
          new BadRequestError(
            JSON.stringify({
              message: "Validation failed",
              errors: errorMessages,
            })
          )
        );
      } else {
        next(error);
      }
    }
  };
}
```

---

## ❌ **File 3: src/middleware/error.middleware.ts**

Global error handler:

```typescript
import { Request, Response, NextFunction } from "express";
import { AppError } from "../utils/errors";
import { ZodError } from "zod";

/**
 * Global error handling middleware
 * Must be placed AFTER all routes
 */
export function errorHandler(
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction
): void {
  // Log error for debugging
  console.error("Error:", err);

  // Handle operational errors (AppError instances)
  if (err instanceof AppError) {
    res.status(err.statusCode).json({
      success: false,
      message: err.message,
      ...(process.env.NODE_ENV === "development" && { stack: err.stack }),
    });
    return;
  }

  // Handle Zod validation errors
  if (err instanceof ZodError) {
    res.status(400).json({
      success: false,
      message: "Validation failed",
      errors: err.errors.map((e) => ({
        field: e.path.join("."),
        message: e.message,
      })),
    });
    return;
  }

  // Handle JWT errors
  if (err.name === "JsonWebTokenError") {
    res.status(401).json({
      success: false,
      message: "Invalid token",
    });
    return;
  }

  if (err.name === "TokenExpiredError") {
    res.status(401).json({
      success: false,
      message: "Token has expired",
    });
    return;
  }

  // Handle database errors (Postgres)
  if (err.message.includes("duplicate key value")) {
    res.status(409).json({
      success: false,
      message: "Resource already exists",
    });
    return;
  }

  // Handle unknown/programming errors
  res.status(500).json({
    success: false,
    message:
      process.env.NODE_ENV === "development"
        ? err.message
        : "Internal server error",
    ...(process.env.NODE_ENV === "development" && { stack: err.stack }),
  });
}

/**
 * Handle 404 - Route not found
 * Should be placed before error handler, after all routes
 */
export function notFoundHandler(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  res.status(404).json({
    success: false,
    message: `Route ${req.method} ${req.originalUrl} not found`,
  });
}
```

---

## 🚦 **File 4: src/middleware/rateLimiter.middleware.ts**

Rate limiting to prevent abuse:

```typescript
import rateLimit from "express-rate-limit";

/**
 * General API rate limiter
 * 100 requests per 15 minutes per IP
 */
export const generalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per window
  message: {
    success: false,
    message: "Too many requests, please try again later",
  },
  standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
  legacyHeaders: false, // Disable the `X-RateLimit-*` headers
});

/**
 * Strict rate limiter for OTP sending
 * 3 requests per hour per IP
 */
export const otpSendLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 3, // Limit each IP to 3 requests per hour
  message: {
    success: false,
    message: "Too many OTP requests, please try again after an hour",
  },
  standardHeaders: true,
  legacyHeaders: false,
  skipSuccessfulRequests: false, // Count all requests, even successful ones
});

/**
 * Rate limiter for OTP verification
 * 5 attempts per 15 minutes per IP
 */
export const otpVerifyLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // Limit each IP to 5 attempts per window
  message: {
    success: false,
    message: "Too many verification attempts, please try again later",
  },
  standardHeaders: true,
  legacyHeaders: false,
});

/**
 * Rate limiter for token refresh
 * 10 requests per minute per IP
 */
export const refreshLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10,
  message: {
    success: false,
    message: "Too many refresh requests, please try again later",
  },
  standardHeaders: true,
  legacyHeaders: false,
});

/**
 * Rate limiter for authentication endpoints (OAuth)
 * 20 requests per 15 minutes per IP
 */
export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 20,
  message: {
    success: false,
    message: "Too many authentication attempts, please try again later",
  },
  standardHeaders: true,
  legacyHeaders: false,
});
```

---

## 📝 **File 5: src/middleware/logger.middleware.ts**

Request logging middleware:

```typescript
import morgan from "morgan";

/**
 * HTTP request logger using morgan
 * Format: :method :url :status :response-time ms - :res[content-length]
 */
export const requestLogger = morgan(
  ":method :url :status :response-time ms - :res[content-length]",
  {
    skip: (req, res) => {
      // Skip logging health check requests to reduce noise
      return req.url === "/health";
    },
  }
);

/**
 * Development logger with more details
 */
export const devLogger = morgan("dev");
```

---

## 🔄 **File 6: src/middleware/index.ts**

Barrel export for all middleware:

```typescript
export * from "./auth.middleware";
export * from "./validation.middleware";
export * from "./error.middleware";
export * from "./rateLimiter.middleware";
export * from "./logger.middleware";
```

---

## 🧪 **Testing Middleware**

Create `src/test-middleware.ts`:

```typescript
import express from "express";
import { authenticateToken, requireRole } from "./middleware/auth.middleware";
import { errorHandler } from "./middleware/error.middleware";
import { generateAccessToken } from "./utils/jwt";

const app = express();
app.use(express.json());

// Test route - protected
app.get("/protected", authenticateToken, (req: any, res) => {
  res.json({
    success: true,
    message: "Access granted",
    user: req.user,
  });
});

// Test route - requires role
app.get("/admin", authenticateToken, requireRole, (req: any, res) => {
  res.json({
    success: true,
    message: "Admin access granted",
    user: req.user,
  });
});

// Error handler
app.use(errorHandler);

// Start server
const PORT = 3001;
app.listen(PORT, () => {
  console.log(`🧪 Test server running on port ${PORT}`);

  // Generate test tokens
  const tokenWithRole = generateAccessToken({
    userId: "test-123",
    role: "creator",
  });
  const tokenWithoutRole = generateAccessToken({
    userId: "test-456",
    role: null,
  });

  console.log("\n📝 Test with these tokens:");
  console.log("\nWith Role:");
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithRole}" http://localhost:${PORT}/protected`
  );
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithRole}" http://localhost:${PORT}/admin`
  );

  console.log("\nWithout Role:");
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithoutRole}" http://localhost:${PORT}/protected`
  );
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithoutRole}" http://localhost:${PORT}/admin`
  );
});
```

Run it:

```bash
npx tsx src/test-middleware.ts
```

Then test with the provided curl commands!

---

## 🎯 **Next Step**

Proceed to **`06_health_check.md`** to create your first API route!
