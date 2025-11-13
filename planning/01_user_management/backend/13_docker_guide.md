# Docker Guide for PostgreSQL

## 🐳 **Overview**

This guide explains how to use Docker to run PostgreSQL for your Agency Management System backend. Using Docker ensures consistent database setup across development, testing, and production environments.

---

## 📋 **Prerequisites**

### Install Docker Desktop

1. **Windows/Mac**: Download from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. **Linux**: Follow instructions at [https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/)

### Verify Installation

```bash
docker --version
# Should show: Docker version 20.10.x or higher

docker-compose --version
# Should show: Docker Compose version v2.x.x or higher
```

---

## 📁 **File: docker-compose.yml**

This file is already created in your `backend/` directory. Here's what it contains:

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:14-alpine
    container_name: agency_platform_db
    restart: unless-stopped
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: agency_platform
      POSTGRES_HOST_AUTH_METHOD: trust
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - agency_network

volumes:
  postgres_data:
    driver: local

networks:
  agency_network:
    driver: bridge
```

### Configuration Explained

| Setting                     | Value              | Purpose                                |
| --------------------------- | ------------------ | -------------------------------------- |
| `image`                     | postgres:14-alpine | Lightweight PostgreSQL 14 image        |
| `container_name`            | agency_platform_db | Easy-to-reference container name       |
| `restart`                   | unless-stopped     | Auto-restart on system reboot          |
| `POSTGRES_USER`             | postgres           | Database superuser username            |
| `POSTGRES_PASSWORD`         | postgres           | Database superuser password            |
| `POSTGRES_DB`               | agency_platform    | Default database name                  |
| `ports`                     | 5432:5432          | Expose PostgreSQL port to host machine |
| `volumes`                   | postgres_data      | Persist database data between restarts |
| `healthcheck`               | pg_isready         | Monitor database availability          |
| `POSTGRES_HOST_AUTH_METHOD` | trust              | Allow connections without password     |

---

## 🚀 **Essential Commands**

### Start PostgreSQL

```bash
# Start in detached mode (runs in background)
docker-compose up -d
```

**Expected Output:**

```
[+] Running 3/3
 ✔ Network backend_agency_network       Created
 ✔ Volume "backend_postgres_data"       Created
 ✔ Container agency_platform_db         Started
```

### Check Status

```bash
docker-compose ps
```

**Expected Output:**

```
NAME                  IMAGE                 STATUS              PORTS
agency_platform_db    postgres:14-alpine    Up 10 seconds       0.0.0.0:5432->5432/tcp
```

### View Logs

```bash
# Follow logs in real-time
docker-compose logs -f postgres

# View last 50 lines
docker-compose logs --tail=50 postgres
```

### Stop PostgreSQL

```bash
# Stop but keep data
docker-compose down

# Stop and remove all data (⚠️ destructive!)
docker-compose down -v
```

### Restart PostgreSQL

```bash
docker-compose restart
```

---

## 🔍 **Database Access**

### Method 1: Using Docker Exec (Recommended)

```bash
# Access psql inside the container
docker-compose exec postgres psql -U postgres -d agency_platform
```

Once inside:

```sql
-- List all databases
\l

-- List all tables
\dt

-- Describe a table
\d users

-- Run a query
SELECT * FROM users;

-- Exit
\q
```

### Method 2: Using pgAdmin or DBeaver

**Connection Details:**

- **Host**: localhost
- **Port**: 5432
- **Username**: postgres
- **Password**: postgres
- **Database**: agency_platform

### Method 3: Using Drizzle Studio

```bash
npm run db:studio
```

Opens at `https://local.drizzle.studio`

---

## 🛠️ **Common Tasks**

### Reset Database Completely

```bash
# Stop and remove all data
docker-compose down -v

# Start fresh
docker-compose up -d

# Re-run migrations
npm run db:push
```

### Backup Database

```bash
# Create backup file
docker-compose exec postgres pg_dump -U postgres agency_platform > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Restore Database

```bash
# Stop current database
docker-compose down

# Remove old data
docker volume rm backend_postgres_data

# Start database
docker-compose up -d

# Wait for database to be ready (5-10 seconds)
sleep 10

# Restore from backup
docker-compose exec -T postgres psql -U postgres agency_platform < backup_20241113_120000.sql
```

### Change Database Password (Production)

Edit `docker-compose.yml`:

```yaml
environment:
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: YOUR_STRONG_PASSWORD_HERE
  POSTGRES_DB: agency_platform
```

Update `.env`:

```env
DATABASE_URL=postgresql://postgres:YOUR_STRONG_PASSWORD_HERE@localhost:5432/agency_platform
```

---

## 🐞 **Troubleshooting**

### Issue: Port 5432 Already in Use

**Symptom:**

```
Error: bind: address already in use
```

**Solution:**

```bash
# Option 1: Stop existing PostgreSQL service
# On Windows:
net stop postgresql-x64-14

# On Mac:
brew services stop postgresql

# On Linux:
sudo systemctl stop postgresql

# Option 2: Change Docker port in docker-compose.yml
ports:
  - "5433:5432"  # Use port 5433 on host

# Then update DATABASE_URL in .env
DATABASE_URL=postgresql://postgres:postgres@localhost:5433/agency_platform
```

### Issue: Container Keeps Restarting

**Check logs:**

```bash
docker-compose logs postgres
```

**Common causes:**

1. Not enough disk space
2. Corrupted data volume
3. Port conflict

**Solution:**

```bash
# Remove and recreate
docker-compose down -v
docker-compose up -d
```

### Issue: "Cannot connect to database"

**Verify container is running:**

```bash
docker-compose ps
# Status should be "Up" not "Restarting"
```

**Test connection:**

```bash
docker-compose exec postgres pg_isready -U postgres
# Should output: /var/run/postgresql:5432 - accepting connections
```

**Check logs:**

```bash
docker-compose logs postgres | tail -20
```

### Issue: Docker Desktop Not Running

**Symptom:**

```
Cannot connect to the Docker daemon
```

**Solution:**

- Open Docker Desktop application
- Wait for it to fully start (green icon in system tray)
- Try command again

---

## 🔐 **Security Best Practices**

### Development

✅ The default configuration is fine:

- Username: postgres
- Password: postgres
- Database: agency_platform

### Production

⚠️ **NEVER use default credentials in production!**

1. **Use strong passwords:**

```yaml
environment:
  POSTGRES_PASSWORD: $(openssl rand -base64 32)
```

2. **Use environment variables:**

```yaml
environment:
  POSTGRES_USER: ${DB_USER}
  POSTGRES_PASSWORD: ${DB_PASSWORD}
  POSTGRES_DB: ${DB_NAME}
```

3. **Restrict network access:**

```yaml
networks:
  agency_network:
    internal: true
```

4. **Use managed database services:**
   - AWS RDS
   - Google Cloud SQL
   - DigitalOcean Managed Databases
   - Azure Database for PostgreSQL

---

## 📊 **Monitoring**

### Check Health Status

```bash
docker-compose ps
# Look for "healthy" status
```

### View Resource Usage

```bash
docker stats agency_platform_db
```

**Sample Output:**

```
CONTAINER           CPU %     MEM USAGE / LIMIT     MEM %
agency_platform_db  0.05%     45.2MiB / 7.775GiB    0.57%
```

### Check Disk Usage

```bash
docker volume ls
docker system df -v
```

---

## 🎯 **Best Practices**

### ✅ DO

- Keep Docker Desktop running during development
- Use `docker-compose down` (without `-v`) to preserve data
- Regularly backup production databases
- Use Docker volumes for data persistence
- Monitor container logs for errors

### ❌ DON'T

- Use `docker-compose down -v` unless you want to delete all data
- Expose PostgreSQL directly to the internet
- Use default credentials in production
- Ignore container restart loops
- Run migrations on production without backups

---

## 📚 **Additional Resources**

### Docker Documentation

- [Docker Compose Reference](https://docs.docker.com/compose/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

### PostgreSQL Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [psql Commands Cheatsheet](https://www.postgresqltutorial.com/postgresql-cheat-sheet/)

---

## 🎓 **Learning Docker Basics**

### Core Concepts

1. **Image**: Blueprint (postgres:14-alpine)
2. **Container**: Running instance (agency_platform_db)
3. **Volume**: Persistent storage (postgres_data)
4. **Network**: Communication channel (agency_network)

### Common Commands

```bash
# List all containers
docker ps -a

# List all images
docker images

# List all volumes
docker volume ls

# List all networks
docker network ls

# Clean up unused resources
docker system prune
```

---

## ✅ **Quick Reference**

| Task                  | Command                                                                         |
| --------------------- | ------------------------------------------------------------------------------- |
| Start database        | `docker-compose up -d`                                                          |
| Stop database         | `docker-compose down`                                                           |
| View logs             | `docker-compose logs -f postgres`                                               |
| Access database shell | `docker-compose exec postgres psql -U postgres -d agency_platform`              |
| Check status          | `docker-compose ps`                                                             |
| Restart               | `docker-compose restart`                                                        |
| Reset all data        | `docker-compose down -v && docker-compose up -d`                                |
| Backup database       | `docker-compose exec postgres pg_dump -U postgres agency_platform > backup.sql` |
| View resource usage   | `docker stats agency_platform_db`                                               |

---

## 🎯 **Next Steps**

Now that PostgreSQL is running:

1. ✅ Verify connection: `npm run db:studio`
2. ✅ Run migrations: `npm run db:push`
3. ✅ Start backend server: `npm run dev`
4. ✅ Test health endpoint: `curl http://localhost:5000/health`

---

**Docker setup complete! 🐳 Ready to build your API! 🚀**
