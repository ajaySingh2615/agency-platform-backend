# Step 08: Service Layer

## Objective

Create service interfaces and implementations containing business logic. This layer sits between controllers and repositories.

---

## Service Layer Best Practices

1. **Separate interface and implementation** for better testing
2. Use `@Service` annotation on implementation
3. Use `@Transactional` for database operations
4. Throw business exceptions (not repository exceptions)
5. Keep business logic out of controllers
6. Validate business rules here

---

## File Creation Summary

Due to the size of service classes, this guide provides the complete structure and key method signatures. You'll create:

**Service Interfaces** (in `service/`):

- AuthService
- UserService
- ProfileService
- RoleService
- SessionService
- KycService
- SocialAuthService

**Service Implementations** (in `service/impl/`):

- AuthServiceImpl
- UserServiceImpl
- ProfileServiceImpl
- RoleServiceImpl
- SessionServiceImpl
- KycServiceImpl
- SocialAuthServiceImpl

---

## Step 8.1: Auth Service

### 8.1.1 AuthService Interface

Create `backend/src/main/java/com/createrapp/service/AuthService.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.request.RefreshTokenRequest;
import com.createrapp.dto.response.AuthResponse;

import java.util.UUID;

/**
 * Authentication service interface
 */
public interface AuthService {

    /**
     * Register a new user with email/password
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login with email/password
     */
    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress, String userAgent);

    /**
     * Login with phone number and OTP
     */
    AuthResponse loginWithPhone(String phoneNumber, String otp, String deviceInfo, String ipAddress, String userAgent);

    /**
     * Send OTP to phone number
     */
    void sendOtp(String phoneNumber);

    /**
     * Verify OTP
     */
    boolean verifyOtp(String phoneNumber, String otp);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user (invalidate session)
     */
    void logout(UUID sessionId);

    /**
     * Logout from all devices
     */
    void logoutAll(UUID userId);

    /**
     * Verify email with token
     */
    void verifyEmail(String token);

    /**
     * Resend email verification
     */
    void resendEmailVerification(String email);

    /**
     * Change password
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);

    /**
     * Reset password request (send email)
     */
    void requestPasswordReset(String email);

    /**
     * Reset password with token
     */
    void resetPassword(String token, String newPassword);
}
```

### 8.1.2 AuthServiceImpl

Create `backend/src/main/java/com/createrapp/service/impl/AuthServiceImpl.java`:

```java
package com.createrapp.service.impl;

import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.request.RefreshTokenRequest;
import com.createrapp.dto.response.AuthResponse;
import com.createrapp.entity.User;
import com.createrapp.entity.UserSession;
import com.createrapp.entity.enums.AccountStatus;
import com.createrapp.exception.BadRequestException;
import com.createrapp.exception.UnauthorizedException;
import com.createrapp.exception.DuplicateResourceException;
import com.createrapp.exception.MaxSessionsExceededException;
import com.createrapp.repository.UserRepository;
import com.createrapp.repository.UserSessionRepository;
import com.createrapp.security.JwtTokenProvider;
import com.createrapp.service.AuthService;
import com.createrapp.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Check if phone already exists (if provided)
        if (request.getPhoneNumber() != null &&
            userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .accountStatus(AccountStatus.PENDING_ONBOARDING)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email (async)
        // emailService.sendVerificationEmail(savedUser);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUserId());

        // Create session
        sessionService.createSession(savedUser.getUserId(), refreshToken, null, null, null);

        log.info("User registered successfully: {}", savedUser.getUserId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .accountStatus(savedUser.getAccountStatus())
                .message("Registration successful")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, String deviceInfo,
                              String ipAddress, String userAgent) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Find user by email or phone
        User user = userRepository.findByEmailOrPhoneNumber(
                        request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check account status
        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new UnauthorizedException("Account is banned");
        }
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Create session (will enforce max 2 sessions)
        sessionService.createSession(user.getUserId(), refreshToken,
                                     deviceInfo, ipAddress, userAgent);

        log.info("User logged in successfully: {}", user.getUserId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .accountStatus(user.getAccountStatus())
                .message("Login successful")
                .build();
    }

    @Override
    public AuthResponse loginWithPhone(String phoneNumber, String otp,
                                       String deviceInfo, String ipAddress, String userAgent) {
        // Implementation for phone login
        // Verify OTP, find user, generate tokens, create session
        throw new UnsupportedOperationException("Phone login not yet implemented");
    }

    @Override
    public void sendOtp(String phoneNumber) {
        // Implementation for sending OTP
        throw new UnsupportedOperationException("OTP sending not yet implemented");
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        // Implementation for OTP verification
        throw new UnsupportedOperationException("OTP verification not yet implemented");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Extract user ID
        UUID userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

        // Verify session exists
        String tokenHash = sessionService.hashToken(request.getRefreshToken());
        UserSession session = sessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Session not found"));

        if (session.isExpired()) {
            sessionRepository.delete(session);
            throw new UnauthorizedException("Session expired");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        log.info("Access token refreshed for user: {}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .userId(userId)
                .message("Token refreshed successfully")
                .build();
    }

    @Override
    public void logout(UUID sessionId) {
        log.info("Logging out session: {}", sessionId);
        sessionService.deleteSession(sessionId);
    }

    @Override
    public void logoutAll(UUID userId) {
        log.info("Logging out all sessions for user: {}", userId);
        sessionService.deleteAllUserSessions(userId);
    }

    @Override
    public void verifyEmail(String token) {
        // Implementation for email verification
        throw new UnsupportedOperationException("Email verification not yet implemented");
    }

    @Override
    public void resendEmailVerification(String email) {
        // Implementation for resending verification email
        throw new UnsupportedOperationException("Resend verification not yet implemented");
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Logout all sessions for security
        logoutAll(userId);

        log.info("Password changed for user: {}", userId);
    }

    @Override
    public void requestPasswordReset(String email) {
        // Implementation for password reset request
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implementation for password reset
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }
}
```

---

## Step 8.2: User Service

### 8.2.1 UserService Interface

Create `backend/src/main/java/com/createrapp/service/UserService.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.response.UserResponse;
import com.createrapp.entity.User;
import com.createrapp.entity.enums.AccountStatus;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Get user by ID
     */
    UserResponse getUserById(UUID userId);

    /**
     * Get user entity by ID (for internal use)
     */
    User getUserEntityById(UUID userId);

    /**
     * Get user by email
     */
    UserResponse getUserByEmail(String email);

    /**
     * Get user by phone number
     */
    UserResponse getUserByPhone(String phoneNumber);

    /**
     * Update user account status
     */
    void updateAccountStatus(UUID userId, AccountStatus status);

    /**
     * Delete user account
     */
    void deleteUser(UUID userId);

    /**
     * Suspend user account
     */
    void suspendUser(UUID userId, String reason);

    /**
     * Reactivate suspended account
     */
    void reactivateUser(UUID userId);

    /**
     * Ban user permanently
     */
    void banUser(UUID userId, String reason);

    /**
     * Get all users (admin only)
     */
    List<UserResponse> getAllUsers();

    /**
     * Search users by criteria
     */
    List<UserResponse> searchUsers(String searchTerm);
}
```

---

## Step 8.3: SessionService

### 8.3.1 SessionService Interface

Create `backend/src/main/java/com/createrapp/service/SessionService.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.response.SessionResponse;
import com.createrapp.entity.UserSession;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    /**
     * Create a new user session
     * Automatically enforces max 2 sessions per user
     */
    UserSession createSession(UUID userId, String refreshToken,
                             String deviceInfo, String ipAddress, String userAgent);

    /**
     * Get all active sessions for a user
     */
    List<SessionResponse> getActiveSessions(UUID userId);

    /**
     * Delete a specific session (logout)
     */
    void deleteSession(UUID sessionId);

    /**
     * Delete all sessions for a user
     */
    void deleteAllUserSessions(UUID userId);

    /**
     * Cleanup expired sessions (scheduled task)
     */
    void cleanupExpiredSessions();

    /**
     * Hash refresh token for storage
     */
    String hashToken(String token);

    /**
     * Verify if session exists and is valid
     */
    boolean isSessionValid(UUID sessionId);

    /**
     * Update session last accessed time
     */
    void updateLastAccessedTime(UUID sessionId);
}
```

### 8.3.2 SessionServiceImpl

Create `backend/src/main/java/com/createrapp/service/impl/SessionServiceImpl.java`:

```java
package com.createrapp.service.impl;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.response.SessionResponse;
import com.createrapp.entity.User;
import com.createrapp.entity.UserSession;
import com.createrapp.exception.MaxSessionsExceededException;
import com.createrapp.exception.ResourceNotFoundException;
import com.createrapp.repository.UserRepository;
import com.createrapp.repository.UserSessionRepository;
import com.createrapp.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpiration;

    @Override
    public UserSession createSession(UUID userId, String refreshToken,
                                     String deviceInfo, String ipAddress, String userAgent) {
        log.info("Creating session for user: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check active session count
        long activeCount = sessionRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());

        if (activeCount >= AppConstants.MAX_SESSIONS_PER_USER) {
            // Delete oldest session
            List<UserSession> oldestSessions = sessionRepository
                    .findOldestActiveSession(userId, LocalDateTime.now());
            if (!oldestSessions.isEmpty()) {
                sessionRepository.delete(oldestSessions.get(0));
                log.info("Deleted oldest session to make room for new session");
            }
        }

        // Hash refresh token
        String tokenHash = hashToken(refreshToken);

        // Create new session
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(tokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        UserSession savedSession = sessionRepository.save(session);
        log.info("Session created: {}", savedSession.getSessionId());

        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(UUID userId) {
        List<UserSession> sessions = sessionRepository
                .findActiveSessionsByUserId(userId, LocalDateTime.now());

        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(UUID sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Session deleted: {}", sessionId);
    }

    @Override
    public void deleteAllUserSessions(UUID userId) {
        sessionRepository.deleteByUser_UserId(userId);
        log.info("All sessions deleted for user: {}", userId);
    }

    @Override
    @Scheduled(cron = "${app.session.cleanup-cron:0 0 */6 * * *}")
    public void cleanupExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up {} expired sessions", deleted);
    }

    @Override
    public String hashToken(String token) {
        return passwordEncoder.encode(token);
    }

    @Override
    public boolean isSessionValid(UUID sessionId) {
        return sessionRepository.isSessionActive(sessionId, LocalDateTime.now());
    }

    @Override
    public void updateLastAccessedTime(UUID sessionId) {
        sessionRepository.updateLastAccessedAt(sessionId, LocalDateTime.now());
    }

    private SessionResponse mapToResponse(UserSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .deviceInfo(session.getDeviceInfo())
                .ipAddress(session.getIpAddress())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .build();
    }
}
```

---

## Step 8.4: Additional Service Stubs

For brevity, I'll provide the interface signatures for remaining services. You can implement them following the same pattern.

### ProfileService Interface

Create `backend/src/main/java/com/createrapp/service/ProfileService.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.request.ProfileUpdateRequest;
import com.createrapp.dto.response.ProfileResponse;
import com.createrapp.entity.enums.RoleName;

import java.util.UUID;

public interface ProfileService {
    ProfileResponse getProfile(UUID userId);
    ProfileResponse createProfile(UUID userId, RoleName roleType, ProfileUpdateRequest request);
    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);
    void deleteProfile(UUID userId);
}
```

### RoleService Interface

Create `backend/src/main/java/com/createrapp/service/RoleService.java`:

```java
package com.createrapp.service;

import com.createrapp.entity.enums.RoleName;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    void assignRole(UUID userId, RoleName roleName);
    void removeRole(UUID userId, RoleName roleName);
    List<RoleName> getUserRoles(UUID userId);
    boolean hasRole(UUID userId, RoleName roleName);
}
```

### KycService Interface

Create `backend/src/main/java/com/createrapp/service/KycService.java`:

```java
package com.createrapp.service;

import com.createrapp.dto.request.KycSubmissionRequest;
import com.createrapp.dto.response.KycResponse;
import com.createrapp.entity.enums.DocumentType;

import java.util.List;
import java.util.UUID;

public interface KycService {
    KycResponse submitDocument(UUID userId, KycSubmissionRequest request);
    KycResponse getDocument(Long documentId);
    List<KycResponse> getUserDocuments(UUID userId);
    void approveDocument(Long documentId, UUID reviewerId);
    void rejectDocument(Long documentId, UUID reviewerId, String reason);
    List<KycResponse> getPendingDocuments();
}
```

---

## Step 8.5: Compile and Verify

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Verification Checklist

- ✅ AuthService interface and implementation created
- ✅ UserService interface created
- ✅ SessionService interface and implementation created
- ✅ ProfileService interface created
- ✅ RoleService interface created
- ✅ KycService interface created
- ✅ All services use `@Service` annotation
- ✅ Implementations use `@Transactional`
- ✅ Business exceptions used appropriately
- ✅ Logging added to important methods
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed Service Layer (Core)**  
➡️ Proceed to **[09_CONTROLLERS.md](./09_CONTROLLERS.md)** to create REST API controllers.
