import { drizzle } from "drizzle-orm/postgres-js";
import { migrate } from "drizzle-orm/postgres-js/migrator";
import postgres from "postgres";
import * as dotenv from "dotenv";

// Load environment variables
dotenv.config();

const connectonString = process.env.DATABASE_URL as string;

if (!connectonString) {
  throw new Error("DATABASE_URL is not defined in environment variables");
}

// Create a migration client (separeate form main app connection)
const migrationClient = postgres(connectonString, { max: 1 });

const db = drizzle(migrationClient);

async function runMigrations() {
  console.log("Running migrations...");

  try {
    await migrate(db, { migrationsFolder: "drizzle" });
    console.log("Migrations completed successfully");
  } catch (error) {
    console.error("Migration failed:", error);
    process.exit(1);
  } finally {
    await migrationClient.end();
  }
}

runMigrations();
