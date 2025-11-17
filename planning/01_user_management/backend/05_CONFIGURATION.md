# Step 05: Application Configuration

## Objective

Configure `application.properties` files for different environments (dev, test, prod) with database, security, JWT, and other settings.

---

## Step 5.1: Main Configuration File

Create/Update `backend/src/main/resources/application.properties`:

```properties
# ============================================
# APPLICATION CONFIGURATION
# ============================================
spring.application.name=CreaterApp Backend
server.port=8080

# Active Profile (dev, test, prod)
spring.profiles.active=dev

# ============================================
# DATABASE CONFIGURATION
# ============================================
# Will be overridden by profile-specific properties
spring.datasource.url=jdbc:postgresql://localhost:5432/createrapp_db
spring.datasource.username=createrapp_user
spring.datasource.password=createrapp_password
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ============================================
# JPA / HIBERNATE CONFIGURATION
# ============================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# ============================================
# LOGGING CONFIGURATION
# ============================================
logging.level.root=INFO
logging.level.com.createrapp=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Logging Pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Log File
logging.file.name=logs/createrapp.log
logging.file.max-size=10MB
logging.file.max-history=30

# ============================================
# JWT CONFIGURATION
# ============================================
# Secret key (should be moved to environment variable in production)
app.jwt.secret=YourSuperSecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
app.jwt.access-token-expiration-ms=900000
app.jwt.refresh-token-expiration-ms=604800000

# 15 minutes for access token
# 7 days for refresh token

# ============================================
# SECURITY CONFIGURATION
# ============================================
# Max concurrent sessions per user
app.security.max-sessions=2

# Password strength
app.security.password.min-length=8
app.security.password.require-uppercase=true
app.security.password.require-lowercase=true
app.security.password.require-digit=true
app.security.password.require-special-char=true

# ============================================
# CORS CONFIGURATION
# ============================================
app.cors.allowed-origins=http://localhost:3000,http://localhost:3001
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
app.cors.max-age=3600

# ============================================
# FILE UPLOAD CONFIGURATION (for KYC)
# ============================================
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# File storage location
app.file.upload-dir=./uploads/kyc
app.file.allowed-extensions=pdf,jpg,jpeg,png

# ============================================
# EMAIL CONFIGURATION (Gmail SMTP)
# ============================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email settings
app.mail.from=noreply@createrapp.com
app.mail.from-name=CreaterApp

# ============================================
# SOCIAL LOGIN CONFIGURATION
# ============================================
# Google OAuth
app.oauth.google.client-id=your-google-client-id
app.oauth.google.client-secret=your-google-client-secret

# Facebook OAuth
app.oauth.facebook.app-id=your-facebook-app-id
app.oauth.facebook.app-secret=your-facebook-app-secret

# Apple Sign In
app.oauth.apple.client-id=your-apple-client-id
app.oauth.apple.team-id=your-apple-team-id
app.oauth.apple.key-id=your-apple-key-id

# ============================================
# CACHE CONFIGURATION
# ============================================
spring.cache.type=simple
spring.cache.cache-names=users,roles,sessions

# ============================================
# ACTUATOR CONFIGURATION (Health Checks)
# ============================================
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.db.enabled=true

# ============================================
# API DOCUMENTATION (Swagger)
# ============================================
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=method

# ============================================
# FLYWAY CONFIGURATION (Optional)
# ============================================
spring.flyway.enabled=false
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# ============================================
# APPLICATION SPECIFIC SETTINGS
# ============================================
app.base-url=http://localhost:8080
app.frontend-url=http://localhost:3000

# OTP Configuration
app.otp.length=6
app.otp.expiration-minutes=5
app.otp.max-attempts=3

# Session cleanup
app.session.cleanup-cron=0 0 */6 * * *

# KYC Settings
app.kyc.auto-approve=false
app.kyc.max-documents-per-user=5
```

---

## Step 5.2: Development Profile

Create `backend/src/main/resources/application-dev.properties`:

```properties
# ============================================
# DEVELOPMENT ENVIRONMENT
# ============================================

# Database (Docker)
spring.datasource.url=jdbc:postgresql://localhost:5432/createrapp_db
spring.datasource.username=createrapp_user
spring.datasource.password=createrapp_password

# JPA Settings
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=validate

# Logging (More verbose)
logging.level.com.createrapp=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# DevTools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Security (Less strict for development)
app.security.password.min-length=6

# CORS (Allow all origins in dev)
app.cors.allowed-origins=*

# JWT (Shorter expiration for testing)
app.jwt.access-token-expiration-ms=3600000

# Email (Console logging instead of actual sending)
spring.mail.host=localhost
spring.mail.port=1025

# Base URLs
app.base-url=http://localhost:8080
app.frontend-url=http://localhost:3000

# Actuator (Show all details)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

---

## Step 5.3: Test Profile

Create `backend/src/main/resources/application-test.properties`:

```properties
# ============================================
# TEST ENVIRONMENT
# ============================================

# In-Memory H2 Database for Testing
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true

# JPA Settings
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Logging (Minimal)
logging.level.com.createrapp=INFO
logging.level.org.springframework=WARN

# JWT (Short expiration for tests)
app.jwt.access-token-expiration-ms=60000
app.jwt.refresh-token-expiration-ms=120000

# Email (Disabled)
spring.mail.host=localhost
spring.mail.port=3025

# File Upload (Temp directory)
app.file.upload-dir=./target/test-uploads

# Cache (None)
spring.cache.type=none
```

---

## Step 5.4: Production Profile (Template)

Create `backend/src/main/resources/application-prod.properties`:

```properties
# ============================================
# PRODUCTION ENVIRONMENT
# ============================================
# WARNING: Use environment variables for sensitive data!

# Database (Use environment variables)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool (Larger pool)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10

# JPA Settings
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate

# Logging (Production level)
logging.level.root=WARN
logging.level.com.createrapp=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate.SQL=WARN

# JWT (Use environment variables)
app.jwt.secret=${JWT_SECRET}
app.jwt.access-token-expiration-ms=900000
app.jwt.refresh-token-expiration-ms=604800000

# Security (Strict)
app.security.password.min-length=10

# CORS (Specific origins only)
app.cors.allowed-origins=${ALLOWED_ORIGINS}

# Email (Production SMTP)
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}

# Social OAuth (Use environment variables)
app.oauth.google.client-id=${GOOGLE_CLIENT_ID}
app.oauth.google.client-secret=${GOOGLE_CLIENT_SECRET}
app.oauth.facebook.app-id=${FACEBOOK_APP_ID}
app.oauth.facebook.app-secret=${FACEBOOK_APP_SECRET}

# File Upload (S3 or cloud storage)
app.file.upload-dir=/var/app/uploads

# Base URLs
app.base-url=${BASE_URL}
app.frontend-url=${FRONTEND_URL}

# Actuator (Restricted)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never

# SSL/TLS
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
```

---

## Step 5.5: Create Configuration Class

Create `backend/src/main/java/com/createrapp/config/AppConstants.java`:

```java
package com.createrapp.config;

/**
 * Application-wide constants
 */
public class AppConstants {

    // API Versioning
    public static final String API_VERSION_V1 = "/api/v1";

    // Pagination Defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // Security
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Session Management
    public static final int MAX_SESSIONS_PER_USER = 2;

    // OTP
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRATION_MINUTES = 5;
    public static final int OTP_MAX_ATTEMPTS = 3;

    // Age Verification
    public static final int MIN_AGE_HOST = 18;

    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png"};
    public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf", "jpg", "jpeg", "png"};

    // Roles
    public static final String ROLE_HOST = "HOST";
    public static final String ROLE_AGENCY = "AGENCY";
    public static final String ROLE_BRAND = "BRAND";
    public static final String ROLE_GIFTER = "GIFTER";
    public static final String ROLE_ADMIN = "ADMIN";

    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}
```

---

## Step 5.6: Environment Variables Setup (Windows)

For local development, create `backend/.env` (gitignored):

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/createrapp_db
DB_USERNAME=createrapp_user
DB_PASSWORD=createrapp_password

# JWT
JWT_SECRET=YourSuperSecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# OAuth
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret

# URLs
BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3000
ALLOWED_ORIGINS=http://localhost:3000
```

To load `.env` in PowerShell:

```powershell
# Load environment variables from .env
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}
```

---

## Step 5.7: Verify Configuration

1. **Check properties files exist**:

   - `application.properties` ✓
   - `application-dev.properties` ✓
   - `application-test.properties` ✓
   - `application-prod.properties` ✓

2. **Test with different profiles**:

```bash
# Run with dev profile (default)
mvn spring-boot:run

# Run with test profile
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

3. **Check application startup logs**:

```
INFO: The following 1 profile is active: "dev"
INFO: Started CreaterAppApplication in X seconds
```

---

## Configuration Best Practices

✅ **DO**:

- Use environment variables for sensitive data
- Keep different profiles for different environments
- Use meaningful property names
- Document all custom properties
- Keep prod secrets out of git

❌ **DON'T**:

- Hardcode passwords in properties files
- Commit `.env` files to git
- Use `ddl-auto=create-drop` in production
- Expose all actuator endpoints in production

---

## Troubleshooting

### Issue 1: Properties not loading

**Solution**: Check file names match exactly: `application-dev.properties`

### Issue 2: Wrong profile active

**Solution**: Set in `application.properties`:

```properties
spring.profiles.active=dev
```

### Issue 3: Environment variables not loading

**Solution**: Set them before starting the application or use IDE run configuration

---

## Verification Checklist

Before moving to the next step:

- ✅ All 4 properties files created
- ✅ `AppConstants.java` created
- ✅ Database connection details configured
- ✅ JWT settings configured
- ✅ CORS settings configured
- ✅ File upload settings configured
- ✅ Application starts without errors
- ✅ Correct profile is active

---

## Next Step

✅ **Completed Configuration**  
➡️ Proceed to **[06_ENTITY_MODELS.md](./06_ENTITY_MODELS.md)** to create JPA entity classes.
