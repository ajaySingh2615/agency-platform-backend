import bcrypt from "bcrypt";
import { randomBytes } from "crypto";

// Salt rounds for bcrypt (12 is a good balance of security and performance)
const SALT_ROUNDS = 12;

// Hash a plain text value using bcrypt
// Used for: OTPs, Refresh Tokens
export async function hashValue(plainText: string): Promise<string> {
  return await bcrypt.hash(plainText, SALT_ROUNDS);
}

// Compare a plain text value with a hashed value
// Returns true if they match
export async function compareHash(
  plainText: string,
  hash: string
): Promise<boolean> {
  return await bcrypt.compare(plainText, hash);
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
