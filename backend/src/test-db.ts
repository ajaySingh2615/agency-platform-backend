import * as dotenv from "dotenv";
dotenv.config();

import { checkDatabaseConnection } from "./config/database";
import { db } from "./db/connection";
import { users } from "./db/schema";

async function testDatabase() {
  console.log("Testing database connection...");

  const isConnected = await checkDatabaseConnection();

  if (!isConnected) {
    console.error("Database connection failed");
    process.exit(1);
  }

  console.log("Database connection successful");

  // Try to query users table
  try {
    const allUsers = await db.select().from(users);
    console.log(`Users table exists. Current count: ${allUsers.length}`);
  } catch (error) {
    console.error("Failed to query users table:", error);
  }

  process.exit(0);
}

testDatabase();
