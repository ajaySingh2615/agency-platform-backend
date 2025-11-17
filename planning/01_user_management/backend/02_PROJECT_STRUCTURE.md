# Step 02: Project Structure

## Objective

Create a professional, scalable project structure following Spring Boot best practices with clear separation of concerns.

---

## Complete Package Structure

Create the following package structure in `src/main/java/com/createrapp/`:

```
com.createrapp/
├── CreaterAppApplication.java          # Main application class
│
├── controller/                         # REST API Controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProfileController.java
│   ├── KycController.java
│   ├── RoleController.java
│   └── SessionController.java
│
├── service/                            # Service Interfaces
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProfileService.java
│   ├── KycService.java
│   ├── RoleService.java
│   ├── SessionService.java
│   ├── SocialAuthService.java
│   └── EmailService.java
│
├── service/impl/                       # Service Implementations
│   ├── AuthServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── ProfileServiceImpl.java
│   ├── KycServiceImpl.java
│   ├── RoleServiceImpl.java
│   ├── SessionServiceImpl.java
│   ├── SocialAuthServiceImpl.java
│   └── EmailServiceImpl.java
│
├── repository/                         # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── SocialIdentityRepository.java
│   ├── RoleRepository.java
│   ├── UserRoleRepository.java
│   ├── ProfileHostRepository.java
│   ├── ProfileAgencyRepository.java
│   ├── ProfileBrandRepository.java
│   ├── ProfileGifterRepository.java
│   ├── UserSessionRepository.java
│   └── KycDocumentRepository.java
│
├── entity/                             # JPA Entities (Database Models)
│   ├── User.java
│   ├── SocialIdentity.java
│   ├── Role.java
│   ├── UserRole.java
│   ├── ProfileHost.java
│   ├── ProfileAgency.java
│   ├── ProfileBrand.java
│   ├── ProfileGifter.java
│   ├── UserSession.java
│   ├── KycDocument.java
│   └── enums/                          # Enums
│       ├── AccountStatus.java
│       ├── SocialProvider.java
│       ├── RoleName.java
│       ├── Gender.java
│       ├── DocumentType.java
│       └── KycStatus.java
│
├── dto/                                # Data Transfer Objects
│   ├── request/                        # Request DTOs
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── SocialLoginRequest.java
│   │   ├── PhoneLoginRequest.java
│   │   ├── VerifyOtpRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── ProfileUpdateRequest.java
│   │   ├── RoleSelectionRequest.java
│   │   └── KycSubmissionRequest.java
│   │
│   └── response/                       # Response DTOs
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── ProfileResponse.java
│       ├── KycResponse.java
│       ├── SessionResponse.java
│       ├── ApiResponse.java
│       └── ErrorResponse.java
│
├── security/                           # Security Configuration
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── CustomUserDetailsService.java
│   ├── CustomUserDetails.java
│   └── SecurityUtils.java
│
├── config/                             # Application Configuration
│   ├── WebConfig.java
│   ├── SwaggerConfig.java
│   ├── AsyncConfig.java
│   ├── CorsConfig.java
│   └── AppConstants.java
│
├── exception/                          # Custom Exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   ├── DuplicateResourceException.java
│   ├── MaxSessionsExceededException.java
│   └── InvalidTokenException.java
│
├── validation/                         # Custom Validators
│   ├── validator/
│   │   ├── PhoneNumberValidator.java
│   │   ├── AgeValidator.java
│   │   └── PasswordValidator.java
│   └── annotation/
│       ├── ValidPhoneNumber.java
│       ├── ValidAge.java
│       └── ValidPassword.java
│
└── util/                              # Utility Classes
    ├── PasswordUtil.java
    ├── DateUtil.java
    ├── StringUtil.java
    └── ResponseUtil.java
```

---

## Resources Structure

Create the following structure in `src/main/resources/`:

```
resources/
├── application.properties              # Main configuration
├── application-dev.properties          # Development profile
├── application-test.properties         # Test profile
├── application-prod.properties         # Production profile
│
├── db/
│   └── migration/                     # Flyway migrations (optional)
│       ├── V1__create_users_table.sql
│       ├── V2__create_social_identities_table.sql
│       ├── V3__create_roles_table.sql
│       └── ...
│
├── static/                            # Static resources (CSS, JS, images)
│   └── .gitkeep
│
└── templates/                         # Email templates (if needed)
    └── .gitkeep
```

---

## Test Structure

Create the following structure in `src/test/java/com/createrapp/`:

```
com.createrapp/
├── CreaterAppApplicationTests.java
│
├── controller/                        # Controller tests
│   ├── AuthControllerTest.java
│   └── UserControllerTest.java
│
├── service/                           # Service tests
│   ├── AuthServiceTest.java
│   └── UserServiceTest.java
│
├── repository/                        # Repository tests
│   ├── UserRepositoryTest.java
│   └── UserSessionRepositoryTest.java
│
└── integration/                       # Integration tests
    ├── AuthIntegrationTest.java
    └── UserIntegrationTest.java
```

---

## How to Create the Structure

### Option 1: Manual Creation (Recommended for learning)

1. Open IntelliJ IDEA
2. Navigate to `src/main/java/com/createrapp`
3. Right-click → New → Package
4. Create each package one by one

### Option 2: Using Command Line (Windows PowerShell)

Navigate to `backend/src/main/java/com/createrapp` and run:

```powershell
# Create main packages
New-Item -ItemType Directory -Path controller
New-Item -ItemType Directory -Path service
New-Item -ItemType Directory -Path "service\impl"
New-Item -ItemType Directory -Path repository
New-Item -ItemType Directory -Path entity
New-Item -ItemType Directory -Path "entity\enums"
New-Item -ItemType Directory -Path dto
New-Item -ItemType Directory -Path "dto\request"
New-Item -ItemType Directory -Path "dto\response"
New-Item -ItemType Directory -Path security
New-Item -ItemType Directory -Path config
New-Item -ItemType Directory -Path exception
New-Item -ItemType Directory -Path validation
New-Item -ItemType Directory -Path "validation\validator"
New-Item -ItemType Directory -Path "validation\annotation"
New-Item -ItemType Directory -Path util
```

For test structure, navigate to `backend/src/test/java/com/createrapp` and run:

```powershell
New-Item -ItemType Directory -Path controller
New-Item -ItemType Directory -Path service
New-Item -ItemType Directory -Path repository
New-Item -ItemType Directory -Path integration
```

For resources, navigate to `backend/src/main/resources` and run:

```powershell
New-Item -ItemType Directory -Path db
New-Item -ItemType Directory -Path "db\migration"
```

---

## Layer Responsibilities

### 1. **Controller Layer**

- Handle HTTP requests and responses
- Validate input using `@Valid`
- Map DTOs to/from entities
- Return appropriate HTTP status codes
- **NO business logic here**

### 2. **Service Layer**

- Contains business logic
- Transaction management (`@Transactional`)
- Orchestrates multiple repository calls
- Handles business validations
- Independent of HTTP/REST concerns

### 3. **Service Implementation Layer**

- Implements service interfaces
- All concrete business logic here
- Easier to mock for testing

### 4. **Repository Layer**

- Data access using Spring Data JPA
- Custom queries using `@Query`
- Database interactions only
- **NO business logic here**

### 5. **Entity Layer**

- JPA entities representing database tables
- Use Lombok to reduce boilerplate
- Define relationships (`@OneToMany`, `@ManyToOne`, etc.)
- Include validation constraints

### 6. **DTO Layer**

- Transfer data between layers
- Prevent exposing entities directly
- Include validation annotations
- Separate request and response DTOs

### 7. **Security Layer**

- Authentication and authorization
- JWT token management
- User details loading
- Security filters

### 8. **Configuration Layer**

- Bean definitions
- Third-party library configs
- Application-wide settings

### 9. **Exception Layer**

- Centralized exception handling
- Custom exception classes
- Standardized error responses

### 10. **Validation Layer**

- Custom validation logic
- Reusable validators
- Annotation-based validation

### 11. **Util Layer**

- Helper methods
- Stateless utility functions
- Common operations

---

## Package Naming Conventions

✅ **DO**:

- Use lowercase package names
- Use plural for collections: `controllers`, `services`
- Group related classes: `dto/request`, `dto/response`
- Use descriptive names: `exception`, not `ex`

❌ **DON'T**:

- Mix case: `Controller`, `CONTROLLER`
- Use abbreviations: `ctrl`, `svc`
- Create deep nesting without purpose

---

## File Naming Conventions

✅ **DO**:

- Controllers: `*Controller.java` (e.g., `AuthController.java`)
- Services: `*Service.java` (interface), `*ServiceImpl.java` (implementation)
- Repositories: `*Repository.java`
- Entities: `*.java` (singular, e.g., `User.java`)
- DTOs: `*Request.java`, `*Response.java`
- Exceptions: `*Exception.java`

---

## Verification Checklist

Before moving to the next step:

- ✅ All packages created under `src/main/java/com/createrapp`
- ✅ Test packages created under `src/test/java/com/createrapp`
- ✅ Resources structure created
- ✅ No compilation errors
- ✅ IDE recognizes all packages

---

## Project Structure Visualization

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/createrapp/        [11 packages]
│   │   └── resources/                  [config files]
│   └── test/
│       └── java/com/createrapp/        [4 test packages]
├── target/                             [build output - generated]
├── pom.xml
└── README.md
```

---

## Next Step

✅ **Completed Project Structure**  
➡️ Proceed to **[03_DEPENDENCIES.md](./03_DEPENDENCIES.md)** to add all required Maven dependencies.
