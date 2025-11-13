# Step 6: Health Check Route & Express App Setup

## 🏥 **File 1: src/routes/health.routes.ts**

Your first API route - Health Check:

```typescript
import { Router, Request, Response } from "express";
import { checkDatabaseConnection } from "../config/database";

const router = Router();

/**
 * GET /health
 * Basic health check - returns server status
 */
router.get("/", async (req: Request, res: Response) => {
  res.status(200).json({
    success: true,
    message: "Server is running",
    timestamp: new Date().toISOString(),
  });
});

/**
 * GET /health/detailed
 * Detailed health check - includes database status
 */
router.get("/detailed", async (req: Request, res: Response) => {
  const dbConnected = await checkDatabaseConnection();

  const status = dbConnected ? 200 : 503;

  res.status(status).json({
    success: dbConnected,
    server: "running",
    database: dbConnected ? "connected" : "disconnected",
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || "development",
  });
});

export default router;
```

---

## 🚀 **File 2: src/app.ts**

Main Express application setup:

```typescript
import express, { Application } from "express";
import helmet from "helmet";
import cors from "cors";
import cookieParser from "cookie-parser";
import * as dotenv from "dotenv";

// Load environment variables
dotenv.config();

// Import middleware
import {
  requestLogger,
  errorHandler,
  notFoundHandler,
  generalLimiter,
} from "./middleware";

// Import routes
import healthRoutes from "./routes/health.routes";

// Create Express app
const app: Application = express();

// =================================================================
// 1. SECURITY MIDDLEWARE
// =================================================================

// Helmet - sets various HTTP headers for security
app.use(helmet());

// CORS - allow cross-origin requests
const allowedOrigins = process.env.ALLOWED_ORIGINS?.split(",") || [
  "http://localhost:5173",
  "http://localhost:3000",
];

app.use(
  cors({
    origin: (origin, callback) => {
      // Allow requests with no origin (mobile apps, Postman, etc.)
      if (!origin) return callback(null, true);

      if (allowedOrigins.includes(origin)) {
        callback(null, true);
      } else {
        callback(new Error("Not allowed by CORS"));
      }
    },
    credentials: true, // Allow cookies
  })
);

// =================================================================
// 2. PARSING MIDDLEWARE
// =================================================================

app.use(express.json({ limit: "10mb" })); // Parse JSON bodies
app.use(express.urlencoded({ extended: true, limit: "10mb" })); // Parse URL-encoded bodies
app.use(cookieParser()); // Parse cookies

// =================================================================
// 3. LOGGING MIDDLEWARE
// =================================================================

app.use(requestLogger);

// =================================================================
// 4. RATE LIMITING
// =================================================================

app.use("/api", generalLimiter); // Apply to all /api routes

// =================================================================
// 5. ROUTES
// =================================================================

// Health check routes (no /api prefix for simplicity)
app.use("/health", healthRoutes);

// TODO: Add more routes here as we build them
// app.use('/api/auth', authRoutes);
// app.use('/api/users', userRoutes);

// =================================================================
// 6. ERROR HANDLING
// =================================================================

// 404 handler - must be after all routes
app.use(notFoundHandler);

// Global error handler - must be last
app.use(errorHandler);

// =================================================================
// 7. EXPORT APP (for server.ts to use)
// =================================================================

export default app;
```

---

## 🌐 **File 3: src/server.ts**

Server entry point (separates app from server for testing):

```typescript
import app from "./app";
import { checkDatabaseConnection } from "./config/database";

const PORT = process.env.PORT || 5000;

/**
 * Start the server
 */
async function startServer() {
  try {
    // Check database connection before starting
    console.log("🔍 Checking database connection...");
    const dbConnected = await checkDatabaseConnection();

    if (!dbConnected) {
      console.error("❌ Failed to connect to database");
      console.error(
        "Make sure PostgreSQL is running and DATABASE_URL is correct"
      );
      process.exit(1);
    }

    console.log("✅ Database connected successfully");

    // Start listening
    app.listen(PORT, () => {
      console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      console.log("🚀 Server is running!");
      console.log(`📍 URL: http://localhost:${PORT}`);
      console.log(`🏥 Health Check: http://localhost:${PORT}/health`);
      console.log(`🌍 Environment: ${process.env.NODE_ENV || "development"}`);
      console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    });
  } catch (error) {
    console.error("❌ Failed to start server:", error);
    process.exit(1);
  }
}

// Handle uncaught exceptions
process.on("uncaughtException", (error) => {
  console.error("❌ Uncaught Exception:", error);
  process.exit(1);
});

// Handle unhandled promise rejections
process.on("unhandledRejection", (reason, promise) => {
  console.error("❌ Unhandled Rejection at:", promise, "reason:", reason);
  process.exit(1);
});

// Start the server
startServer();
```

---

## 📁 **File 4: Update package.json scripts**

Ensure your `package.json` has these scripts:

```json
{
  "scripts": {
    "dev": "tsx watch src/server.ts",
    "build": "tsc",
    "start": "node dist/server.js",
    "db:generate": "drizzle-kit generate:pg",
    "db:push": "drizzle-kit push:pg",
    "db:studio": "drizzle-kit studio",
    "db:migrate": "tsx src/db/migrate.ts"
  }
}
```

---

## 🧪 **Testing Your Server**

### Step 1: Start the server

```bash
npm run dev
```

You should see:

```
🔍 Checking database connection...
✅ Database connected successfully
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚀 Server is running!
📍 URL: http://localhost:5000
🏥 Health Check: http://localhost:5000/health
🌍 Environment: development
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Step 2: Test Health Check Routes

**Basic Health Check:**

```bash
curl http://localhost:5000/health
```

Expected response:

```json
{
  "success": true,
  "message": "Server is running",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

**Detailed Health Check:**

```bash
curl http://localhost:5000/health/detailed
```

Expected response:

```json
{
  "success": true,
  "server": "running",
  "database": "connected",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "environment": "development"
}
```

### Step 3: Test 404 Handler

```bash
curl http://localhost:5000/nonexistent
```

Expected response:

```json
{
  "success": false,
  "message": "Route GET /nonexistent not found"
}
```

### Step 4: Test CORS (from browser console)

Open browser console and run:

```javascript
fetch("http://localhost:5000/health")
  .then((res) => res.json())
  .then((data) => console.log(data))
  .catch((err) => console.error(err));
```

Should work without CORS errors!

---

## 🎨 **Optional: Create a Postman Collection**

Create `postman_collection.json`:

```json
{
  "info": {
    "name": "Agency Platform - User Management",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/health",
          "host": ["{{baseUrl}}"],
          "path": ["health"]
        }
      }
    },
    {
      "name": "Detailed Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/health/detailed",
          "host": ["{{baseUrl}}"],
          "path": ["health", "detailed"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:5000"
    }
  ]
}
```

Import this into Postman to easily test your API!

---

## ✅ **Verification Checklist**

- [ ] Server starts without errors
- [ ] Database connection is successful
- [ ] `/health` endpoint returns 200 OK
- [ ] `/health/detailed` shows database as connected
- [ ] Invalid routes return 404 with proper JSON
- [ ] CORS is working (test from browser)
- [ ] Request logging appears in console
- [ ] Environment variables are loaded correctly

---

## 🎯 **Next Step**

Now that your server is running, proceed to **`07_phone_authentication.md`** to implement phone OTP authentication!
