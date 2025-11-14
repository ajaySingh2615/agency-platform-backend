// Get expiry date for OTP (default: 5 minutes from now)
export function getOtpExpiryDate(minutes: number = 5): Date {
  const expiry = new Date();
  expiry.setMinutes(expiry.getMinutes() + minutes);
  return expiry;
}

// Get expiry date for refresh token (default: 30 days from now)
export function getRefreshTokenExpiryDate(days: number = 30): Date {
  const expiry = new Date();
  expiry.setDate(expiry.getDate() + days);
  return expiry;
}

// Check if a date has expired
export function isExpired(expiryDate: Date): boolean {
  return new Date() > expiryDate;
}

// Format date for logging
export function formatDate(date: Date): string {
  return date.toISOString();
}
