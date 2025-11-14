import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import * as dotenv from "dotenv";
import * as schema from "./schema";

// Load environment variables
dotenv.config();

// Get database URL from environment variables
const connectionString = process.env.DATABASE_URL;

if (!connectionString) {
  throw new Error("DATABASE_URL is not defined in environment variables");
}

// Create PostgreSQL connection
// maxLifeTime: 30 seconds (connections are recycled)
// max: 10 (connection pool size)
const client = postgres(connectionString, {
  max: 10,
  idle_timeout: 20,
  connect_timeout: 10,
});

// create drizzle ORM instance with schema
export const db = drizzle(client, { schema });

// Export schema for user in other files
export { schema };
