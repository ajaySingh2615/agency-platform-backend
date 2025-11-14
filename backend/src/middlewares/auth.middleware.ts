import { Request, Response, NextFunction } from "express";
import { extractBearerToken, verifyAccessToken } from "../utils/jwt";
import { ForbiddenError, UnauthorizedError } from "../utils/errors";

// Extended Express Request with user information
export interface AuthRequest extends Request {
  user?: {
    userId: string;
    role: string | null;
  };
}

// Middleware to verify JWT access token
// Adds user info to request object if valid
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

// Middleware to check if user has selected a role
// Must be used AFTER authenticateToken
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

// Middleware to check if user has one of the allowed roles
// Must be used AFTER authenticateToken
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

// Optional authentication - adds user to request if token exists, but doesn't fail if missing
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
    next(error);
  }
}
