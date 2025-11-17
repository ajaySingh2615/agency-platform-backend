# Step 04: Database Setup with Docker

## Objective

Set up PostgreSQL database using Docker for local development and create the initial database schema.

---

## Step 4.1: Install Docker Desktop

1. Download Docker Desktop from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Install and restart your computer
3. Verify installation:

```bash
docker --version
docker-compose --version
```

---

## Step 4.2: Create docker-compose.yml

Create `backend/docker-compose.yml`:

```yaml
version: "3.8"

services:
  # PostgreSQL Database
  postgres:
    image: postgres:16-alpine
    container_name: createrapp-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: createrapp_db
      POSTGRES_USER: createrapp_user
      POSTGRES_PASSWORD: createrapp_password
      PGDATA: /var/lib/postgresql/data/pgdata
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - createrapp-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U createrapp_user -d createrapp_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  # pgAdmin (Optional - Database Management UI)
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: createrapp-pgadmin
    restart: unless-stopped
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@createrapp.com
      PGADMIN_DEFAULT_PASSWORD: admin123
      PGADMIN_LISTEN_PORT: 80
    ports:
      - "5050:80"
    volumes:
      - pgadmin_data:/var/lib/pgadmin
    networks:
      - createrapp-network
    depends_on:
      - postgres

volumes:
  postgres_data:
    driver: local
  pgadmin_data:
    driver: local

networks:
  createrapp-network:
    driver: bridge
```

---

## Step 4.3: Create Database Initialization Script

Create folder structure: `backend/docker/postgres/init/`

Create `backend/docker/postgres/init/01-init.sql`:

```sql
-- ============================================
-- Creater App Database Initialization
-- Phase 1: User Management Module
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. CORE IDENTITY TABLES
-- ============================================

-- Table: users (Master Record)
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_phone_verified BOOLEAN DEFAULT FALSE,
    account_status VARCHAR(50) DEFAULT 'PENDING_ONBOARDING'
        CHECK (account_status IN ('PENDING_ONBOARDING', 'ACTIVE', 'SUSPENDED', 'BANNED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT chk_email_or_phone CHECK (email IS NOT NULL OR phone_number IS NOT NULL)
);

-- Table: social_identities
CREATE TABLE social_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL CHECK (provider IN ('GOOGLE', 'FACEBOOK', 'APPLE')),
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provider, provider_user_id)
);

-- ============================================
-- 2. ACCESS CONTROL TABLES
-- ============================================

-- Table: roles (Static Table)
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
        CHECK (role_name IN ('HOST', 'AGENCY', 'BRAND', 'GIFTER', 'ADMIN')),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (role_name, description) VALUES
    ('HOST', 'Content creator/streamer role'),
    ('AGENCY', 'Agency managing multiple hosts'),
    ('BRAND', 'Brand/advertiser for promotions'),
    ('GIFTER', 'User who can send gifts'),
    ('ADMIN', 'System administrator');

-- Table: user_roles (Many-to-Many)
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================
-- 3. DOMAIN PROFILES TABLES
-- ============================================

-- Table: profile_hosts (Creators)
CREATE TABLE profile_hosts (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    display_name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    dob DATE NOT NULL,
    bio TEXT,
    profile_pic_url VARCHAR(500),
    onboarding_step INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_age CHECK (DATE_PART('year', AGE(dob)) >= 18)
);

-- Table: profile_agencies
CREATE TABLE profile_agencies (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100),
    contact_person VARCHAR(100),
    agency_code VARCHAR(50) UNIQUE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: profile_brands
CREATE TABLE profile_brands (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    brand_name VARCHAR(255) NOT NULL,
    website_url VARCHAR(500),
    industry VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: profile_gifters
CREATE TABLE profile_gifters (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    level INTEGER DEFAULT 1,
    vip_status BOOLEAN DEFAULT FALSE,
    total_spent DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. SECURITY & COMPLIANCE TABLES
-- ============================================

-- Table: user_sessions (Max 2 Devices)
CREATE TABLE user_sessions (
    session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: kyc_documents
CREATE TABLE kyc_documents (
    document_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL
        CHECK (document_type IN ('PASSPORT', 'NATIONAL_ID', 'PAN', 'BUSINESS_LICENSE', 'DRIVING_LICENSE')),
    document_url VARCHAR(500) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    verified_at TIMESTAMP,
    reviewer_id UUID REFERENCES users(user_id)
);

-- ============================================
-- 5. INDEXES FOR PERFORMANCE
-- ============================================

-- Users table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_status ON users(account_status);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Social identities indexes
CREATE INDEX idx_social_identities_user_id ON social_identities(user_id);
CREATE INDEX idx_social_identities_provider ON social_identities(provider, provider_user_id);

-- User roles indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Profiles indexes
CREATE INDEX idx_profile_hosts_user_id ON profile_hosts(user_id);
CREATE INDEX idx_profile_agencies_user_id ON profile_agencies(user_id);
CREATE INDEX idx_profile_agencies_code ON profile_agencies(agency_code);
CREATE INDEX idx_profile_brands_user_id ON profile_brands(user_id);
CREATE INDEX idx_profile_gifters_user_id ON profile_gifters(user_id);

-- Sessions indexes
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(refresh_token_hash);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- KYC documents indexes
CREATE INDEX idx_kyc_documents_user_id ON kyc_documents(user_id);
CREATE INDEX idx_kyc_documents_status ON kyc_documents(status);

-- ============================================
-- 6. TRIGGERS FOR UPDATED_AT
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_social_identities_updated_at BEFORE UPDATE ON social_identities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_hosts_updated_at BEFORE UPDATE ON profile_hosts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_agencies_updated_at BEFORE UPDATE ON profile_agencies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_brands_updated_at BEFORE UPDATE ON profile_brands
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_gifters_updated_at BEFORE UPDATE ON profile_gifters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- 7. FUNCTION TO ENFORCE MAX 2 SESSIONS
-- ============================================

CREATE OR REPLACE FUNCTION enforce_max_sessions()
RETURNS TRIGGER AS $$
DECLARE
    session_count INTEGER;
    oldest_session UUID;
BEGIN
    -- Count active sessions for this user
    SELECT COUNT(*) INTO session_count
    FROM user_sessions
    WHERE user_id = NEW.user_id AND expires_at > CURRENT_TIMESTAMP;

    -- If already at max (2), delete the oldest one
    IF session_count >= 2 THEN
        SELECT session_id INTO oldest_session
        FROM user_sessions
        WHERE user_id = NEW.user_id AND expires_at > CURRENT_TIMESTAMP
        ORDER BY created_at ASC
        LIMIT 1;

        DELETE FROM user_sessions WHERE session_id = oldest_session;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger
CREATE TRIGGER trigger_enforce_max_sessions
    BEFORE INSERT ON user_sessions
    FOR EACH ROW EXECUTE FUNCTION enforce_max_sessions();

-- ============================================
-- END OF INITIALIZATION
-- ============================================
```

---

## Step 4.4: Start PostgreSQL Container

```bash
# Navigate to backend folder
cd backend

# Start containers
docker-compose up -d

# Check if containers are running
docker ps

# View logs
docker-compose logs -f postgres
```

Expected output:

```
✔ Container createrapp-postgres  Running
✔ Container createrapp-pgadmin   Running
```

---

## Step 4.5: Verify Database Connection

### Option 1: Using psql (Command Line)

```bash
# Connect to database
docker exec -it createrapp-postgres psql -U createrapp_user -d createrapp_db

# Inside psql, list tables
\dt

# Should show all 11 tables
# Exit psql
\q
```

### Option 2: Using pgAdmin (Web UI)

1. Open browser: `http://localhost:5050`
2. Login:
   - Email: `admin@createrapp.com`
   - Password: `admin123`
3. Add New Server:
   - Name: `CreaterApp DB`
   - Host: `postgres` (service name in docker-compose)
   - Port: `5432`
   - Database: `createrapp_db`
   - Username: `createrapp_user`
   - Password: `createrapp_password`
4. Browse tables in the left panel

---

## Step 4.6: Database Management Commands

```bash
# Start database
docker-compose up -d

# Stop database
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# View logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres

# Execute SQL file
docker exec -i createrapp-postgres psql -U createrapp_user -d createrapp_db < your-script.sql

# Backup database
docker exec createrapp-postgres pg_dump -U createrapp_user createrapp_db > backup.sql

# Restore database
docker exec -i createrapp-postgres psql -U createrapp_user -d createrapp_db < backup.sql
```

---

## Step 4.7: Create .env File (Optional - for sensitive data)

Create `backend/.env`:

```properties
# Database Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=createrapp_db
POSTGRES_USER=createrapp_user
POSTGRES_PASSWORD=createrapp_password

# pgAdmin Configuration
PGADMIN_EMAIL=admin@createrapp.com
PGADMIN_PASSWORD=admin123
```

Add to `backend/.gitignore`:

```
.env
```

---

## Database Schema Summary

After initialization, you will have:

### Tables Created: 11

1. ✅ `users` - Core user data
2. ✅ `social_identities` - Social login links
3. ✅ `roles` - Role definitions (5 roles inserted)
4. ✅ `user_roles` - User-to-role mapping
5. ✅ `profile_hosts` - Host/Creator profiles
6. ✅ `profile_agencies` - Agency profiles
7. ✅ `profile_brands` - Brand profiles
8. ✅ `profile_gifters` - Gifter profiles
9. ✅ `user_sessions` - Session management
10. ✅ `kyc_documents` - KYC verification

### Indexes Created: 15+

- Optimized queries for email, phone, user_id, etc.

### Triggers Created: 8

- Auto-update `updated_at` timestamps
- Enforce max 2 concurrent sessions

### Constraints:

- Foreign keys for referential integrity
- Check constraints for valid enum values
- Unique constraints for email, phone, tokens
- Age verification (18+) for hosts

---

## Troubleshooting

### Issue 1: Port 5432 already in use

**Solution**: Change port mapping in `docker-compose.yml`:

```yaml
ports:
  - "5433:5432" # Use 5433 on host
```

### Issue 2: Docker containers not starting

**Solution**:

```bash
# Check Docker Desktop is running
# Restart Docker Desktop
# Remove old containers
docker-compose down -v
docker-compose up -d
```

### Issue 3: Permission denied errors

**Solution**:

```bash
# Run PowerShell as Administrator
# Or check folder permissions
```

### Issue 4: Init script not executing

**Solution**:

```bash
# Remove volume and recreate
docker-compose down -v
docker volume rm backend_postgres_data
docker-compose up -d
```

---

## Verification Checklist

Before moving to the next step:

- ✅ Docker Desktop installed and running
- ✅ `docker-compose.yml` created
- ✅ Init script created in `docker/postgres/init/01-init.sql`
- ✅ Containers running: `docker ps` shows postgres and pgadmin
- ✅ Database accessible via psql or pgAdmin
- ✅ All 11 tables created successfully
- ✅ 5 roles inserted in `roles` table
- ✅ Indexes and triggers created

---

## Next Step

✅ **Completed Database Setup**  
➡️ Proceed to **[05_CONFIGURATION.md](./05_CONFIGURATION.md)** to configure application properties.
