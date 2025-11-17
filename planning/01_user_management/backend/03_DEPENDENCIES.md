# Step 03: Maven Dependencies

## Objective

Add all required dependencies to `pom.xml` for building a production-grade Spring Boot application with PostgreSQL, Security, JWT, Validation, and more.

---

## Complete pom.xml

Replace the content of `backend/pom.xml` with the following:

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
        <!-- Java Version -->
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Dependency Versions -->
        <jwt.version>0.12.3</jwt.version>
        <springdoc.version>2.3.0</springdoc.version>
        <lombok.version>1.18.30</lombok.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <commons-lang3.version>3.14.0</commons-lang3.version>
        <google-auth.version>1.19.0</google-auth.version>
    </properties>

    <dependencies>

        <!-- ============================================ -->
        <!-- SPRING BOOT STARTERS                          -->
        <!-- ============================================ -->

        <!-- Spring Boot Web (REST APIs) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA (ORM/Hibernate) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Boot Mail (for Email verification) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>

        <!-- Spring Boot Actuator (Health checks, metrics) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Spring Boot Cache (for session management optimization) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- ============================================ -->
        <!-- DATABASE                                      -->
        <!-- ============================================ -->

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway (Database Migration) - Optional but recommended -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- HikariCP (Connection Pooling - already included in Spring Boot) -->

        <!-- ============================================ -->
        <!-- SECURITY & JWT                                -->
        <!-- ============================================ -->

        <!-- JWT Library (JJWT) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- BCrypt (Password Hashing - included in Spring Security) -->

        <!-- ============================================ -->
        <!-- SOCIAL LOGIN                                  -->
        <!-- ============================================ -->

        <!-- Google OAuth Client -->
        <dependency>
            <groupId>com.google.auth</groupId>
            <artifactId>google-auth-library-oauth2-http</artifactId>
            <version>${google-auth.version}</version>
        </dependency>

        <!-- For Facebook/Apple, you may need additional libraries -->
        <!-- We'll add those when implementing social login -->

        <!-- ============================================ -->
        <!-- UTILITIES                                     -->
        <!-- ============================================ -->

        <!-- Lombok (Reduce Boilerplate Code) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- MapStruct (DTO Mapping) -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- Apache Commons Lang3 (String utilities, etc.) -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>${commons-lang3.version}</version>
        </dependency>

        <!-- ============================================ -->
        <!-- API DOCUMENTATION                             -->
        <!-- ============================================ -->

        <!-- SpringDoc OpenAPI (Swagger UI) -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- ============================================ -->
        <!-- TESTING                                       -->
        <!-- ============================================ -->

        <!-- Spring Boot Test Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Spring Security Test -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- H2 Database (For Testing) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- TestContainers (For Integration Testing with Real PostgreSQL) -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>

        <!-- ============================================ -->
        <!-- DEV TOOLS                                     -->
        <!-- ============================================ -->

        <!-- Spring Boot DevTools (Hot Reload) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- ============================================ -->
        <!-- OPTIONAL - FILE UPLOAD (for KYC documents)    -->
        <!-- ============================================ -->

        <!-- Apache Commons FileUpload -->
        <dependency>
            <groupId>commons-fileupload</groupId>
            <artifactId>commons-fileupload</artifactId>
            <version>1.5</version>
        </dependency>

    </dependencies>

    <!-- ============================================ -->
    <!-- BUILD PLUGINS                                 -->
    <!-- ============================================ -->

    <build>
        <plugins>

            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- Maven Compiler Plugin (for MapStruct) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <!-- Lombok -->
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <!-- MapStruct -->
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <!-- Lombok + MapStruct binding -->
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <!-- Maven Surefire Plugin (for running tests) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>

        </plugins>
    </build>

</project>
```

---

## Dependency Breakdown

### 1. **Spring Boot Starters** (Core)

- `spring-boot-starter-web`: REST APIs, Embedded Tomcat
- `spring-boot-starter-data-jpa`: Database access with Hibernate
- `spring-boot-starter-security`: Authentication & Authorization
- `spring-boot-starter-validation`: Bean validation (JSR-380)
- `spring-boot-starter-mail`: Email sending
- `spring-boot-starter-actuator`: Health checks, metrics
- `spring-boot-starter-cache`: Caching support

### 2. **Database**

- `postgresql`: PostgreSQL JDBC driver
- `flyway-core`: Database schema versioning (optional)
- HikariCP: Connection pooling (included in Spring Boot)

### 3. **Security & JWT**

- `jjwt-api/impl/jackson`: JWT token generation and validation
- BCrypt: Password hashing (included in Spring Security)

### 4. **Social Login**

- `google-auth-library-oauth2-http`: Google OAuth verification
- Additional libraries for Facebook/Apple (to be added later)

### 5. **Utilities**

- `lombok`: Reduces boilerplate (@Getter, @Setter, etc.)
- `mapstruct`: DTO-Entity mapping
- `commons-lang3`: String utilities, null checks

### 6. **API Documentation**

- `springdoc-openapi`: Swagger UI for API testing

### 7. **Testing**

- `spring-boot-starter-test`: JUnit, Mockito, AssertJ
- `spring-security-test`: Security testing utilities
- `h2`: In-memory database for unit tests
- `testcontainers`: Real PostgreSQL for integration tests

### 8. **Dev Tools**

- `spring-boot-devtools`: Hot reload during development

### 9. **File Upload**

- `commons-fileupload`: File upload handling for KYC documents

---

## Dependency Update After Installation

After adding dependencies, run:

```bash
# Clean and update dependencies
mvn clean install

# Or just download dependencies
mvn dependency:resolve
```

---

## IDE Integration

### IntelliJ IDEA

1. Right-click `pom.xml` → Maven → Reload Project
2. Or: Ctrl+Shift+O (Reload Maven Projects)
3. Enable Annotation Processing:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### Eclipse

1. Right-click project → Maven → Update Project
2. Install Lombok plugin from Eclipse Marketplace

---

## Verify Dependencies

Check if dependencies are downloaded:

```bash
# List all dependencies
mvn dependency:tree

# Check for conflicts
mvn dependency:analyze
```

---

## Common Issues & Solutions

### Issue 1: Lombok not working

**Symptom**: `@Data`, `@Getter`, `@Setter` not recognized  
**Solution**:

1. Install Lombok plugin in IDE
2. Enable annotation processing
3. Restart IDE

### Issue 2: MapStruct not generating mappers

**Symptom**: Mapper implementation not found  
**Solution**:

1. Check `maven-compiler-plugin` configuration
2. Run `mvn clean compile`
3. Check `target/generated-sources/annotations/`

### Issue 3: PostgreSQL driver not found

**Symptom**: `ClassNotFoundException: org.postgresql.Driver`  
**Solution**:

1. Run `mvn clean install`
2. Check `~/.m2/repository/org/postgresql/`

### Issue 4: Spring Boot version conflicts

**Symptom**: Dependency resolution errors  
**Solution**:

1. Use versions managed by Spring Boot parent POM
2. Remove explicit versions where possible
3. Run `mvn dependency:tree` to check conflicts

---

## Dependency Management Best Practices

✅ **DO**:

- Use Spring Boot's dependency management
- Keep versions in `<properties>` section
- Use `<scope>test</scope>` for test dependencies
- Regularly update dependencies for security patches

❌ **DON'T**:

- Add unnecessary dependencies
- Use snapshot versions in production
- Mix different versions of related libraries

---

## Security Considerations

- Keep JWT library updated (known vulnerabilities)
- Use latest PostgreSQL driver
- Update Spring Security regularly
- Check for vulnerable dependencies:

```bash
mvn dependency-check:check
```

---

## Verification Checklist

Before moving to the next step:

- ✅ `pom.xml` updated with all dependencies
- ✅ `mvn clean install` runs successfully
- ✅ No dependency conflicts
- ✅ Lombok annotation processing enabled
- ✅ MapStruct configured properly
- ✅ IDE recognizes all dependencies

---

## Next Step

✅ **Completed Dependency Setup**  
➡️ Proceed to **[04_DATABASE_SETUP.md](./04_DATABASE_SETUP.md)** to set up PostgreSQL with Docker.
