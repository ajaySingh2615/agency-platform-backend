# Step 07: Repository Layer (Spring Data JPA)

## Objective

Create repository interfaces using Spring Data JPA for database operations. These provide CRUD operations and custom queries.

---

## Repository Best Practices

1. Extend `JpaRepository<Entity, ID>` for basic CRUD
2. Use method naming conventions for auto-generated queries
3. Use `@Query` for complex queries
4. Add `@Modifying` for update/delete operations
5. Use `Optional<>` for findBy methods to handle null

---

## Step 7.1: Core Identity Repositories

### 7.1.1 UserRepository

Create `backend/src/main/java/com/createrapp/repository/UserRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.User;
import com.createrapp.entity.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by phone number
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Find by email or phone number
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if phone number exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Find by account status
    List<User> findByAccountStatus(AccountStatus status);

    // Find users created after a certain date
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // Find verified email users
    List<User> findByIsEmailVerifiedTrue();

    // Find verified phone users
    List<User> findByIsPhoneVerifiedTrue();

    // Custom query: Update last login timestamp
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    int updateLastLoginAt(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    // Custom query: Update account status
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.userId = :userId")
    int updateAccountStatus(@Param("userId") UUID userId, @Param("status") AccountStatus status);

    // Custom query: Verify email
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true WHERE u.userId = :userId")
    int verifyEmail(@Param("userId") UUID userId);

    // Custom query: Verify phone
    @Modifying
    @Query("UPDATE User u SET u.isPhoneVerified = true WHERE u.userId = :userId")
    int verifyPhone(@Param("userId") UUID userId);

    // Find users with specific role
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.role.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") com.createrapp.entity.enums.RoleName roleName);
}
```

### 7.1.2 SocialIdentityRepository

Create `backend/src/main/java/com/createrapp/repository/SocialIdentityRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.SocialIdentity;
import com.createrapp.entity.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialIdentityRepository extends JpaRepository<SocialIdentity, Long> {

    // Find by provider and provider user ID
    Optional<SocialIdentity> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    // Find all social identities for a user
    List<SocialIdentity> findByUser_UserId(UUID userId);

    // Find by user and provider
    Optional<SocialIdentity> findByUser_UserIdAndProvider(UUID userId, SocialProvider provider);

    // Check if provider identity exists
    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    // Find by email
    Optional<SocialIdentity> findByEmail(String email);

    // Delete all by user
    void deleteByUser_UserId(UUID userId);
}
```

---

## Step 7.2: Access Control Repositories

### 7.2.1 RoleRepository

Create `backend/src/main/java/com/createrapp/repository/RoleRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.Role;
import com.createrapp.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // Find by role name
    Optional<Role> findByRoleName(RoleName roleName);

    // Check if role exists
    boolean existsByRoleName(RoleName roleName);
}
```

### 7.2.2 UserRoleRepository

Create `backend/src/main/java/com/createrapp/repository/UserRoleRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.UserRole;
import com.createrapp.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {

    // Find all roles for a user
    List<UserRole> findByUser_UserId(UUID userId);

    // Find all users with a specific role
    List<UserRole> findByRole_RoleName(RoleName roleName);

    // Check if user has a specific role
    boolean existsByUser_UserIdAndRole_RoleName(UUID userId, RoleName roleName);

    // Delete user role
    void deleteByUser_UserIdAndRole_RoleId(UUID userId, Integer roleId);

    // Count roles per user
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // Get role names for a user
    @Query("SELECT ur.role.roleName FROM UserRole ur WHERE ur.user.userId = :userId")
    List<RoleName> findRoleNamesByUserId(@Param("userId") UUID userId);
}
```

---

## Step 7.3: Profile Repositories

### 7.3.1 ProfileHostRepository

Create `backend/src/main/java/com/createrapp/repository/ProfileHostRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.ProfileHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileHostRepository extends JpaRepository<ProfileHost, Long> {

    // Find by user ID
    Optional<ProfileHost> findByUser_UserId(UUID userId);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find by display name (for search)
    List<ProfileHost> findByDisplayNameContainingIgnoreCase(String displayName);

    // Find verified hosts
    List<ProfileHost> findByIsVerifiedTrue();

    // Find by onboarding step
    List<ProfileHost> findByOnboardingStep(Integer step);

    // Find incomplete onboarding
    @Query("SELECT ph FROM ProfileHost ph WHERE ph.onboardingStep < 5")
    List<ProfileHost> findIncompleteOnboarding();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
```

### 7.3.2 ProfileAgencyRepository

Create `backend/src/main/java/com/createrapp/repository/ProfileAgencyRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.ProfileAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileAgencyRepository extends JpaRepository<ProfileAgency, Long> {

    // Find by user ID
    Optional<ProfileAgency> findByUser_UserId(UUID userId);

    // Find by agency code
    Optional<ProfileAgency> findByAgencyCode(String agencyCode);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Check if agency code exists
    boolean existsByAgencyCode(String agencyCode);

    // Find by company name
    List<ProfileAgency> findByCompanyNameContainingIgnoreCase(String companyName);

    // Find verified agencies
    List<ProfileAgency> findByIsVerifiedTrue();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
```

### 7.3.3 ProfileBrandRepository

Create `backend/src/main/java/com/createrapp/repository/ProfileBrandRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.ProfileBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileBrandRepository extends JpaRepository<ProfileBrand, Long> {

    // Find by user ID
    Optional<ProfileBrand> findByUser_UserId(UUID userId);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find by brand name
    List<ProfileBrand> findByBrandNameContainingIgnoreCase(String brandName);

    // Find by industry
    List<ProfileBrand> findByIndustry(String industry);

    // Find verified brands
    List<ProfileBrand> findByIsVerifiedTrue();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
```

### 7.3.4 ProfileGifterRepository

Create `backend/src/main/java/com/createrapp/repository/ProfileGifterRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.ProfileGifter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileGifterRepository extends JpaRepository<ProfileGifter, Long> {

    // Find by user ID
    Optional<ProfileGifter> findByUser_UserId(UUID userId);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find VIP gifters
    List<ProfileGifter> findByVipStatusTrue();

    // Find by level
    List<ProfileGifter> findByLevel(Integer level);

    // Find gifters by level range
    List<ProfileGifter> findByLevelBetween(Integer minLevel, Integer maxLevel);

    // Update total spent
    @Modifying
    @Query("UPDATE ProfileGifter pg SET pg.totalSpent = pg.totalSpent + :amount WHERE pg.user.userId = :userId")
    int updateTotalSpent(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    // Update level
    @Modifying
    @Query("UPDATE ProfileGifter pg SET pg.level = :level WHERE pg.user.userId = :userId")
    int updateLevel(@Param("userId") UUID userId, @Param("level") Integer level);

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
```

---

## Step 7.4: Security Repositories

### 7.4.1 UserSessionRepository

Create `backend/src/main/java/com/createrapp/repository/UserSessionRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    // Find by refresh token hash
    Optional<UserSession> findByRefreshTokenHash(String tokenHash);

    // Find all sessions for a user
    List<UserSession> findByUser_UserId(UUID userId);

    // Find active sessions for a user (not expired)
    @Query("SELECT s FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    // Count active sessions for a user
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Find oldest session for a user
    @Query("SELECT s FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    List<UserSession> findOldestActiveSession(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Delete expired sessions
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    // Delete all sessions for a user
    @Modifying
    void deleteByUser_UserId(UUID userId);

    // Update last accessed time
    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessedAt = :time WHERE s.sessionId = :sessionId")
    int updateLastAccessedAt(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

    // Check if session exists and is active
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM UserSession s " +
           "WHERE s.sessionId = :sessionId AND s.expiresAt > :now")
    boolean isSessionActive(@Param("sessionId") UUID sessionId, @Param("now") LocalDateTime now);
}
```

### 7.4.2 KycDocumentRepository

Create `backend/src/main/java/com/createrapp/repository/KycDocumentRepository.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.KycDocument;
import com.createrapp.entity.enums.DocumentType;
import com.createrapp.entity.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    // Find all documents for a user
    List<KycDocument> findByUser_UserId(UUID userId);

    // Find by user and document type
    Optional<KycDocument> findByUser_UserIdAndDocumentType(UUID userId, DocumentType documentType);

    // Find by status
    List<KycDocument> findByStatus(KycStatus status);

    // Find pending documents
    List<KycDocument> findByStatusOrderBySubmittedAtAsc(KycStatus status);

    // Find documents by user and status
    List<KycDocument> findByUser_UserIdAndStatus(UUID userId, KycStatus status);

    // Check if user has approved document
    boolean existsByUser_UserIdAndStatus(UUID userId, KycStatus status);

    // Count documents by user
    long countByUser_UserId(UUID userId);

    // Approve document
    @Modifying
    @Query("UPDATE KycDocument k SET k.status = 'APPROVED', k.verifiedAt = :verifiedAt, " +
           "k.reviewer.userId = :reviewerId WHERE k.documentId = :documentId")
    int approveDocument(
            @Param("documentId") Long documentId,
            @Param("reviewerId") UUID reviewerId,
            @Param("verifiedAt") LocalDateTime verifiedAt
    );

    // Reject document
    @Modifying
    @Query("UPDATE KycDocument k SET k.status = 'REJECTED', k.rejectionReason = :reason, " +
           "k.reviewedAt = :reviewedAt, k.reviewer.userId = :reviewerId WHERE k.documentId = :documentId")
    int rejectDocument(
            @Param("documentId") Long documentId,
            @Param("reviewerId") UUID reviewerId,
            @Param("reason") String reason,
            @Param("reviewedAt") LocalDateTime reviewedAt
    );

    // Find documents submitted after date
    List<KycDocument> findBySubmittedAtAfter(LocalDateTime date);

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
```

---

## Step 7.5: Compile and Verify

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Repository Testing (Optional - for verification)

Create a simple test in `src/test/java/com/createrapp/repository/UserRepositoryTest.java`:

```java
package com.createrapp.repository;

import com.createrapp.entity.User;
import com.createrapp.entity.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        // Create user
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .accountStatus(AccountStatus.PENDING_ONBOARDING)
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .build();

        // Save
        User savedUser = userRepository.save(user);

        // Find
        User foundUser = userRepository.findByEmail("test@example.com").orElse(null);

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getUserId()).isNotNull();
    }
}
```

Run test:

```bash
mvn test -Dtest=UserRepositoryTest
```

---

## Verification Checklist

- ✅ UserRepository created with custom queries
- ✅ SocialIdentityRepository created
- ✅ RoleRepository created
- ✅ UserRoleRepository created with composite key support
- ✅ All 4 profile repositories created
- ✅ UserSessionRepository with session management queries
- ✅ KycDocumentRepository with approval/rejection methods
- ✅ All repositories extend JpaRepository
- ✅ Custom queries use `@Query` annotation
- ✅ Modifying queries use `@Modifying` annotation
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed Repository Layer**  
➡️ Proceed to **[08_SERVICES.md](./08_SERVICES.md)** to create service layer with business logic.
