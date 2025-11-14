import { console } from "inspector";
import { db } from "../db/connection";
import { sql } from "drizzle-orm";

// check if database connection is healthy
export async function checkDatabaseConnection(): Promise<boolean> {
  try {
    // Simple query to test connection
    await db.execute(sql`SELECT 1`);
    return true;
  } catch (error) {
    console.error("Datbase connection failed: ", error);
    return false;
  }
}

// Clean up expired OTPs (should be run periodically)
export async function cleanupExpiredOtps(): Promise<void> {
  const { phoneOtps } = await import("../db/schema");
  try {
    await db.delete(phoneOtps).where(sql`${phoneOtps.expiresAt} < NOW()`);
    console.log("✅ Expired OTPs cleaned up");
  } catch (error) {
    console.error("❌ Failed to cleanup OTPs:", error);
  }
}

// Clean up expired refresh tokens (should be run periodically)
export async function cleanupExpiredTokens(): Promise<void> {
  const { userRefreshTokens } = await import("../db/schema");
  try {
    await db
      .delete(userRefreshTokens)
      .where(sql`${userRefreshTokens.expiresAt} < NOW()`);
    console.log("Expired refresh tokens cleaned up");
  } catch (error) {
    console.error("Failed to cleanup tokens:", error);
  }
}

// Run all cleanup tasks
export async function runCleanupTasks(): Promise<void> {
  await Promise.all([cleanupExpiredOtps(), cleanupExpiredTokens()]);
}
