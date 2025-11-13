# Drizzle-Kit 0.31.6 Migration Guide

## 📋 **Overview**

When we updated `drizzle-kit` from version `0.20.6` to `0.31.6` (to fix security vulnerabilities), the configuration API changed. This guide explains the breaking changes and how we fixed them.

---

## ⚠️ **What Changed**

### Breaking Changes in drizzle-kit 0.31.6

Drizzle-kit introduced a new configuration API that's incompatible with the old format:

| Aspect            | Old API (0.20.x)                          | New API (0.31.x)                       |
| ----------------- | ----------------------------------------- | -------------------------------------- |
| Import            | `import type { Config }`                  | `import { defineConfig }`              |
| Export            | `export default { ... } satisfies Config` | `export default defineConfig({ ... })` |
| Database type     | `driver: "pg"`                            | `dialect: "postgresql"`                |
| Connection string | `dbCredentials: { connectionString }`     | `dbCredentials: { url }`               |

---

## 🔧 **The Fix**

### Before (0.20.6) - ❌ Broken

```typescript
import type { Config } from "drizzle-kit";
import * as dotenv from "dotenv";

dotenv.config();

export default {
  schema: "./src/db/schema.ts",
  out: "./drizzle",
  driver: "pg", // ❌ Error: '"pg"' is not assignable
  dbCredentials: {
    connectionString: process.env.DATABASE_URL!, // ❌ Error: 'connectionString' does not exist
  },
  verbose: true,
  strict: true,
} satisfies Config;
```

### After (0.31.6) - ✅ Fixed

```typescript
import { defineConfig } from "drizzle-kit";
import * as dotenv from "dotenv";

dotenv.config();

export default defineConfig({
  schema: "./src/db/schema.ts",
  out: "./drizzle",
  dialect: "postgresql", // ✅ New: Use "postgresql" instead of "pg"
  dbCredentials: {
    url: process.env.DATABASE_URL!, // ✅ New: Use "url" instead of "connectionString"
  },
  verbose: true,
  strict: true,
});
```

---

## 📝 **Detailed Changes**

### 1. Import Statement

**Before:**

```typescript
import type { Config } from "drizzle-kit";
```

**After:**

```typescript
import { defineConfig } from "drizzle-kit";
```

**Why:** The new version provides a `defineConfig` helper function that offers better type safety and autocomplete.

---

### 2. Export Format

**Before:**

```typescript
export default {
  // config
} satisfies Config;
```

**After:**

```typescript
export default defineConfig({
  // config
});
```

**Why:** Using `defineConfig()` provides better TypeScript inference and validates the configuration at compile time.

---

### 3. Database Driver → Dialect

**Before:**

```typescript
driver: "pg";
```

**After:**

```typescript
dialect: "postgresql";
```

**Why:** Drizzle now supports multiple database adapters, and the naming convention changed to be more explicit:

| Old Driver | New Dialect    |
| ---------- | -------------- |
| `"pg"`     | `"postgresql"` |
| `"mysql"`  | `"mysql"`      |
| `"sqlite"` | `"sqlite"`     |

---

### 4. Database Credentials

**Before:**

```typescript
dbCredentials: {
  connectionString: process.env.DATABASE_URL!,
}
```

**After:**

```typescript
dbCredentials: {
  url: process.env.DATABASE_URL!,
}
```

**Why:** The property name changed from `connectionString` to `url` for consistency across different database types.

---

## ✅ **Verification**

### Test the Configuration

```bash
# Check for TypeScript errors
npx tsc --noEmit
# Should output: (nothing) ✅

# Test drizzle-kit commands
npx drizzle-kit --help
# Should show available commands ✅
```

### Verify Your .env File

Make sure you have:

```env
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/agency_platform
```

---

## 🚀 **Using Drizzle-Kit Commands**

All commands remain the same, only the configuration format changed:

```bash
# Generate migrations
npm run db:generate

# Push schema to database
npm run db:push

# Open Drizzle Studio
npm run db:studio

# Run migrations programmatically
npm run db:migrate
```

---

## 📚 **Additional Configuration Options**

### Optional Settings

```typescript
export default defineConfig({
  schema: "./src/db/schema.ts",
  out: "./drizzle",
  dialect: "postgresql",
  dbCredentials: {
    url: process.env.DATABASE_URL!,
  },

  // Optional settings
  verbose: true, // Show detailed SQL queries
  strict: true, // Enable strict mode

  // Migration settings (optional)
  migrations: {
    table: "migrations", // Custom migrations table name
    schema: "public", // Schema for migrations table
  },

  // Breakpoints (optional)
  breakpoints: true, // Add breakpoints in migration files
});
```

---

## 🐛 **Troubleshooting**

### Error: "Type '"pg"' is not assignable"

**Cause:** Using old driver format

**Fix:** Change `driver: "pg"` to `dialect: "postgresql"`

---

### Error: "'connectionString' does not exist"

**Cause:** Using old credential format

**Fix:** Change `connectionString` to `url`

---

### Error: "Cannot find module 'drizzle-kit'"

**Cause:** Package not installed

**Fix:**

```bash
npm install drizzle-kit@latest --save-dev
```

---

### Error: "Cannot find name 'defineConfig'"

**Cause:** Old import statement

**Fix:** Change `import type { Config }` to `import { defineConfig }`

---

## 📖 **Official Documentation**

For more details, refer to:

- [Drizzle-Kit Documentation](https://orm.drizzle.team/kit-docs/overview)
- [Drizzle-Kit Configuration](https://orm.drizzle.team/kit-docs/conf)
- [Drizzle-Kit Changelog](https://github.com/drizzle-team/drizzle-orm/releases)

---

## ✅ **Current Status**

| Check                 | Status | Version   |
| --------------------- | ------ | --------- |
| drizzle-kit installed | ✅ Yes | 0.31.6    |
| Configuration updated | ✅ Yes | New API   |
| TypeScript errors     | ✅ 0   | All fixed |
| Commands working      | ✅ Yes | Verified  |

---

## 🎯 **Next Steps**

1. ✅ Configuration updated
2. ✅ TypeScript compiles without errors
3. ✅ Ready to use database commands

You can now proceed with:

- `npm run db:generate` - Generate migration files
- `npm run db:push` - Push schema to database
- `npm run db:studio` - Open database GUI

---

**Migration complete! 🎉 Ready to work with the database! 🚀**
