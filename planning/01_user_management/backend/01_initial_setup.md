# Step 01: Initial Project Setup

## Objective

Create a new Spring Boot project with Java 25 and Maven, configured for professional development.

---

## Step 1.1: Create Project Using Spring Initializr

### Option A: Using Web Interface (Recommended for beginners)

1. Go to [https://start.spring.io/](https://start.spring.io/)
2. Configure the project:

   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.2.0 (or latest stable)
   - **Group**: `com.createrapp`
   - **Artifact**: `backend`
   - **Name**: `CreaterApp`
   - **Package name**: `com.createrapp`
   - **Packaging**: Jar
   - **Java**: 21 (Note: Use 21 for now as 25 support is limited, we'll configure later)

3. Click "Generate" and download the zip file
4. Extract to `C:\Users\cadt1\Documents\creater-app\backend`

### Option B: Using Command Line (Recommended for advanced users)

Open PowerShell in `C:\Users\cadt1\Documents\creater-app\backend` and run:

```bash
curl https://start.spring.io/starter.zip ^
  -d type=maven-project ^
  -d language=java ^
  -d bootVersion=3.2.0 ^
  -d baseDir=. ^
  -d groupId=com.createrapp ^
  -d artifactId=backend ^
  -d name=CreaterApp ^
  -d packageName=com.createrapp ^
  -d packaging=jar ^
  -d javaVersion=21 ^
  -o project.zip

tar -xf project.zip
del project.zip
```

---

## Step 1.2: Verify Java Installation

```bash
# Check Java version
java -version

# Should show Java 21 or higher
# If you have JDK 25, make sure JAVA_HOME is set correctly
```

Set JAVA_HOME if needed:

```bash
# Windows PowerShell
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-25", "User")
```

---

## Step 1.3: Verify Maven Installation

```bash
# Check Maven version
mvn -version

# Should show Maven 3.9+ and point to correct Java version
```

---

## Step 1.4: Initial Project Structure

After initialization, you should have:

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── createrapp/
│   │   │           └── CreaterAppApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/
│               └── createrapp/
│                   └── CreaterAppApplicationTests.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## Step 1.5: Update pom.xml (Basic Configuration)

Open `backend/pom.xml` and verify/update:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.createrapp</groupId>
    <artifactId>backend</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>CreaterApp Backend</name>
    <description>User Management Module for Creater App</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- Dependencies will be added in next step -->
    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Step 1.6: Verify Build

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Step 1.7: Run Initial Application (Smoke Test)

```bash
mvn spring-boot:run
```

You should see:

```
Started CreaterAppApplication in X seconds
```

Press `Ctrl+C` to stop.

---

## Step 1.8: Create .gitignore (If not present)

Create/update `backend/.gitignore`:

```
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
.mvn/

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
*.class

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Application
application-local.properties
application-secrets.properties

# Other
.env
```

---

## Step 1.9: IDE Setup (IntelliJ IDEA Recommended)

1. Open IntelliJ IDEA
2. File → Open → Select `backend` folder
3. Wait for Maven to import dependencies
4. File → Project Structure → Project SDK → Select JDK 21 (or 25)
5. Enable annotation processing: Settings → Build → Compiler → Annotation Processors → Enable

---

## Verification Checklist

Before moving to the next step, verify:

- ✅ Java 21+ installed and configured
- ✅ Maven 3.9+ installed
- ✅ Project created with correct package structure
- ✅ `pom.xml` is valid and builds successfully
- ✅ Application starts without errors
- ✅ IDE properly configured and recognizes the project

---

## Troubleshooting

### Issue: Maven command not found

**Solution**: Add Maven to PATH or use the Maven wrapper (`./mvnw` or `mvnw.cmd`)

### Issue: Wrong Java version

**Solution**: Set JAVA_HOME and update PATH to point to JDK 21+

### Issue: Port 8080 already in use

**Solution**: Stop the application using that port or change the port in `application.properties`:

```properties
server.port=8081
```

---

## Next Step

✅ **Completed Initial Setup**  
➡️ Proceed to **[02_PROJECT_STRUCTURE.md](./02_PROJECT_STRUCTURE.md)** to create the complete folder structure.
