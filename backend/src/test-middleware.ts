import * as dotenv from "dotenv";
dotenv.config();

import express from "express";
import { authenticateToken, requireRole } from "./middlewares/auth.middleware";
import { errorHandler } from "./middlewares/error.middleware";
import { generateAccessToken } from "./utils/jwt";

const app = express();
app.use(express.json());

// Test route - protected
app.get("/protected", authenticateToken, (req: any, res) => {
  res.json({
    success: true,
    message: "Access granted",
    user: req.user,
  });
});

// Test route - requires role
app.get("/admin", authenticateToken, requireRole, (req: any, res) => {
  res.json({
    success: true,
    message: "Admin access granted",
    user: req.user,
  });
});

// Error handler
app.use(errorHandler);

// Start server
const PORT = 3001;
app.listen(PORT, () => {
  console.log(`🧪 Test server running on port ${PORT}`);

  // Generate test tokens
  const tokenWithRole = generateAccessToken({
    userId: "test-123",
    role: "creator",
  });
  const tokenWithoutRole = generateAccessToken({
    userId: "test-456",
    role: null,
  });

  console.log("\n📝 Test with these tokens:");
  console.log("\nWith Role:");
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithRole}" http://localhost:${PORT}/protected`
  );
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithRole}" http://localhost:${PORT}/admin`
  );

  console.log("\nWithout Role:");
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithoutRole}" http://localhost:${PORT}/protected`
  );
  console.log(
    `curl -H "Authorization: Bearer ${tokenWithoutRole}" http://localhost:${PORT}/admin`
  );
});
