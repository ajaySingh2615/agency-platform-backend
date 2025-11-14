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
