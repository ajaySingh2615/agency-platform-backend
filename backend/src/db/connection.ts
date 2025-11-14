import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import * as schema from "./schema";

// Get database URL form environment variables
const connectonString = process.env.DATABASE_URL as string;

if (!connectonString) {
  throw new Error("DATABASE_URL is not defined in environment variables");
}

// Create PostgreSQL connection
// maxLifeTime: 30 seconds (connections are recycled)
// max: 10 (connection pool size)
const client = postgres(connectonString, {
  max: 10,
  idle_timeout: 20,
  connect_timeout: 10,
});

// create drizzle ORM isntance with schema
export const db = drizzle(client, { schema });

// Export schema for user in other files
export { schema };
