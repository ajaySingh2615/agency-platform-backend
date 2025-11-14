import jwt, { SignOptions } from "jsonwebtoken";

// Type definitions for JWT payloads
export interface AccessTokenPayload {
  userId: string;
  role: string | null;
}

export interface RefreshTokenPayload {
  userId: string;
  tokenId: string; // Reference to the row in userRefreshTokens table
}

// Generate an access token (short-lived, 15 minutes)
export function generateAccessToken(payload: AccessTokenPayload): string {
  const secret = process.env.ACCESS_TOKEN_SECRET;
  const expiry = (process.env.ACCESS_TOKEN_EXPIRY ||
    "15m") as SignOptions["expiresIn"];

  if (!secret) {
    throw new Error("ACCESS_TOKEN_SECRET is not defined");
  }

  return jwt.sign(payload, secret, {
    expiresIn: expiry,
    issuer: "agency-platform",
    audience: "agency-platform-users",
  });
}

// Generate a refresh token (long-lived, 30 days)
export function generateRefreshToken(payload: RefreshTokenPayload): string {
  const secret = process.env.REFRESH_TOKEN_SECRET;
  const expiry = (process.env.REFRESH_TOKEN_EXPIRY ||
    "30d") as SignOptions["expiresIn"];

  if (!secret) {
    throw new Error("REFRESH_TOKEN_SECRET is not defined");
  }

  return jwt.sign(payload, secret, {
    expiresIn: expiry,
    issuer: "agency-platform",
    audience: "agency-platform-users",
  });
}

// Verify and decode an access token
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
    if (error instanceof jwt.JsonWebTokenError) {
      throw new Error("Invalid access token");
    }
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error("Access token has expired");
    }
    throw error;
  }
}

// Verify and decode a refresh token
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
    if (error instanceof jwt.JsonWebTokenError) {
      throw new Error("Invalid refresh token");
    }
    if (error instanceof jwt.TokenExpiredError) {
      throw new Error("Refresh token has expired");
    }
    throw error;
  }
}

// Extract token from Authorization header
// Expected format: "Bearer <token>"
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
