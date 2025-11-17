# Step 06: Entity Models (JPA Entities)

## Objective

Create JPA entity classes that map to database tables. These entities represent the domain model of the application.

---

## Important Notes Before Starting

1. **Use Lombok** to reduce boilerplate code
2. **Follow naming conventions**: Class names = Singular, Table names = Plural
3. **Add validation annotations** where appropriate
4. **Define relationships** correctly
5. **Include audit fields** (createdAt, updatedAt)

---

## Step 6.1: Create Enum Classes

### 6.1.1 AccountStatus Enum

Create `backend/src/main/java/com/createrapp/entity/enums/AccountStatus.java`:

```java
package com.createrapp.entity.enums;

/**
 * User account status states
 */
public enum AccountStatus {
    PENDING_ONBOARDING,  // User registered but hasn't completed onboarding
    ACTIVE,              // Active user account
    SUSPENDED,           // Temporarily suspended (can be reactivated)
    BANNED               // Permanently banned
}
```

### 6.1.2 SocialProvider Enum

Create `backend/src/main/java/com/createrapp/entity/enums/SocialProvider.java`:

```java
package com.createrapp.entity.enums;

/**
 * Supported social login providers
 */
public enum SocialProvider {
    GOOGLE,
    FACEBOOK,
    APPLE
}
```

### 6.1.3 RoleName Enum

Create `backend/src/main/java/com/createrapp/entity/enums/RoleName.java`:

```java
package com.createrapp.entity.enums;

/**
 * User roles in the system
 */
public enum RoleName {
    HOST,      // Content creator/streamer
    AGENCY,    // Agency managing hosts
    BRAND,     // Brand/advertiser
    GIFTER,    // User who sends gifts
    ADMIN      // System administrator
}
```

### 6.1.4 Gender Enum

Create `backend/src/main/java/com/createrapp/entity/enums/Gender.java`:

```java
package com.createrapp.entity.enums;

/**
 * Gender options
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}
```

### 6.1.5 DocumentType Enum

Create `backend/src/main/java/com/createrapp/entity/enums/DocumentType.java`:

```java
package com.createrapp.entity.enums;

/**
 * KYC document types
 */
public enum DocumentType {
    PASSPORT,
    NATIONAL_ID,
    PAN,
    BUSINESS_LICENSE,
    DRIVING_LICENSE
}
```

### 6.1.6 KycStatus Enum

Create `backend/src/main/java/com/createrapp/entity/enums/KycStatus.java`:

```java
package com.createrapp.entity.enums;

/**
 * KYC verification status
 */
public enum KycStatus {
    PENDING,   // Submitted, awaiting review
    APPROVED,  // Verified and approved
    REJECTED   // Rejected with reason
}
```

---

## Step 6.2: Core Identity Entities

### 6.2.1 User Entity

Create `backend/src/main/java/com/createrapp/entity/User.java`:

```java
package com.createrapp.entity;

import com.createrapp.entity.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @UuidGenerator  // Modern Hibernate 6.2+ approach (replaces deprecated @GenericGenerator)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Email(message = "Invalid email format")
    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 50)
    private AccountStatus accountStatus = AccountStatus.PENDING_ONBOARDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SocialIdentity> socialIdentities = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileHost profileHost;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileAgency profileAgency;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileBrand profileBrand;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileGifter profileGifter;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserSession> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KycDocument> kycDocuments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addRole(UserRole role) {
        userRoles.add(role);
        role.setUser(this);
    }

    public void removeRole(UserRole role) {
        userRoles.remove(role);
        role.setUser(null);
    }
}
```

### 6.2.2 SocialIdentity Entity

Create `backend/src/main/java/com/createrapp/entity/SocialIdentity.java`:

```java
package com.createrapp.entity;

import com.createrapp.entity.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_identities",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## Step 6.3: Access Control Entities

### 6.3.1 Role Entity

Create `backend/src/main/java/com/createrapp/entity/Role.java`:

```java
package com.createrapp.entity;

import com.createrapp.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private RoleName roleName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 6.3.2 UserRole Entity

Create `backend/src/main/java/com/createrapp/entity/UserRole.java`:

```java
package com.createrapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    // Composite Key Class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRoleId implements Serializable {
        private UUID user;
        private Integer role;
    }
}
```

---

## Step 6.4: Profile Entities

### 6.4.1 ProfileHost Entity

Create `backend/src/main/java/com/createrapp/entity/ProfileHost.java`:

```java
package com.createrapp.entity;

import com.createrapp.entity.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_hosts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Display name is required")
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_pic_url", length = 500)
    private String profilePicUrl;

    @Column(name = "onboarding_step")
    private Integer onboardingStep = 0;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 6.4.2 ProfileAgency Entity

Create `backend/src/main/java/com/createrapp/entity/ProfileAgency.java`:

```java
package com.createrapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_agencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Company name is required")
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "agency_code", nullable = false, unique = true, length = 50)
    private String agencyCode;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 6.4.3 ProfileBrand Entity

Create `backend/src/main/java/com/createrapp/entity/ProfileBrand.java`:

```java
package com.createrapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Brand name is required")
    @Column(name = "brand_name", nullable = false, length = 255)
    private String brandName;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 6.4.4 ProfileGifter Entity

Create `backend/src/main/java/com/createrapp/entity/ProfileGifter.java`:

```java
package com.createrapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_gifters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileGifter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "level")
    private Integer level = 1;

    @Column(name = "vip_status")
    private Boolean vipStatus = false;

    @Column(name = "total_spent", precision = 15, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## Step 6.5: Security Entities

### 6.5.1 UserSession Entity

Create `backend/src/main/java/com/createrapp/entity/UserSession.java`:

```java
package com.createrapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @UuidGenerator  // Modern Hibernate 6.2+ approach (replaces deprecated @GenericGenerator)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    private String refreshTokenHash;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

### 6.5.2 KycDocument Entity

Create `backend/src/main/java/com/createrapp/entity/KycDocument.java`:

```java
package com.createrapp.entity;

import com.createrapp.entity.enums.DocumentType;
import com.createrapp.entity.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private KycStatus status = KycStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
```

---

## Step 6.6: Compile and Verify

```bash
# Navigate to backend folder
cd backend

# Compile the project
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Verification Checklist

- ✅ All 6 enum classes created
- ✅ User entity created with relationships
- ✅ SocialIdentity entity created
- ✅ Role and UserRole entities created
- ✅ All 4 profile entities created
- ✅ UserSession entity created
- ✅ KycDocument entity created
- ✅ All entities use Lombok annotations
- ✅ All entities have @PrePersist/@PreUpdate hooks
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed Entity Models**  
➡️ Proceed to **[07_REPOSITORIES.md](./07_REPOSITORIES.md)** to create Spring Data JPA repositories.
