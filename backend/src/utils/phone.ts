/**
 * Normalize phone number to E.164 format
 * Handles common formats and adds country code if missing
 *
 * Examples:
 * - "9876543210" (India) → "+919876543210"
 * - "01234567890" (India) → "+911234567890"
 * - "+919876543210" → "+919876543210" (already correct)
 */

export function normalizePhoneNumber(
  phone: string,
  defaultCountryCode: string = "91" // India by default
): string {
  // Remove all non-digit characters
  let cleaned = phone.replace(/\D/g, "");

  // If it starts with country code, add + and return
  if (phone.startsWith("+")) {
    return "+" + cleaned;
  }

  // If it starts with 0, remove it (common in India: 09876543210)
  if (cleaned.startsWith("0")) {
    cleaned = cleaned.substring(1);
  }

  // If no country code, add the default one
  if (!cleaned.startsWith(defaultCountryCode)) {
    cleaned = defaultCountryCode + cleaned;
  }

  return "+" + cleaned;
}

// Validate if a phone number is in proper E.164 format
export function isValidE164(phone: string): boolean {
  const e164Regex = /^\+[1-9]\d{1,14}$/;
  return e164Regex.test(phone);
}

// Mask phone number for display
// Example: "+919876543210" → "+91******3210"
export function maskPhoneNumber(phone: string): string {
  if (phone.length < 8) {
    return phone;
  }
  const countryCode = phone.substring(0, 3);
  const lastFour = phone.substring(phone.length - 4);
  const masked = "*".repeat(phone.length - 7);

  return countryCode + masked + lastFour;
}
