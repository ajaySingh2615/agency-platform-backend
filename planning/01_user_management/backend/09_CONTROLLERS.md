# Step 09: REST API Controllers

## Objective

Create REST API controllers that handle HTTP requests, validate input, and return appropriate responses.

---

## Controller Best Practices

1. Use `@RestController` instead of `@Controller`
2. Use `@RequestMapping` for base path
3. Use proper HTTP methods (@GetMapping, @PostMapping, etc.)
4. Use `@Valid` for request validation
5. Return `ResponseEntity<>` for flexible responses
6. Use proper HTTP status codes
7. Add API documentation with Swagger annotations

---

## Step 9.1: Auth Controller

Create `backend/src/main/java/com/createrapp/controller/AuthController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.request.LoginRequest;
import com.createrapp.dto.request.RegisterRequest;
import com.createrapp.dto.request.RefreshTokenRequest;
import com.createrapp.dto.response.ApiResponse;
import com.createrapp.dto.response.AuthResponse;
import com.createrapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register with email/phone and password")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email/phone and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = extractDeviceInfo(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, deviceInfo, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout from current session")
    public ResponseEntity<ApiResponse> logout(
            @RequestParam UUID sessionId) {

        authService.logout(sessionId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Logged out successfully")
                        .build()
        );
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Invalidate all user sessions")
    public ResponseEntity<ApiResponse> logoutAll(
            @RequestParam UUID userId) {

        authService.logoutAll(userId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Logged out from all devices")
                        .build()
        );
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Send OTP to phone number")
    public ResponseEntity<ApiResponse> sendOtp(
            @RequestParam String phoneNumber) {

        authService.sendOtp(phoneNumber);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .build()
        );
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email with token")
    public ResponseEntity<ApiResponse> verifyEmail(
            @RequestParam String token) {

        authService.verifyEmail(token);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Email verified successfully")
                        .build()
        );
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    public ResponseEntity<ApiResponse> resendVerification(
            @RequestParam String email) {

        authService.resendEmailVerification(email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Verification email sent")
                        .build()
        );
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestParam UUID userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestParam String email) {

        authService.requestPasswordReset(email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset email sent")
                        .build()
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset successful")
                        .build()
        );
    }

    // Helper methods
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // Parse user agent to extract device info (simplified)
        if (userAgent != null) {
            if (userAgent.contains("Android")) {
                return "Android Device";
            } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                return "iOS Device";
            } else {
                return "Web Browser";
            }
        }
        return "Unknown Device";
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
```

---

## Step 9.2: User Controller

Create `backend/src/main/java/com/createrapp/controller/UserController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.response.ApiResponse;
import com.createrapp.dto.response.UserResponse;
import com.createrapp.entity.enums.AccountStatus;
import com.createrapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user details by email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Get user by phone", description = "Retrieve user details by phone number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByPhone(@PathVariable String phoneNumber) {
        UserResponse response = userService.getUserByPhone(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by criteria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String searchTerm) {
        List<UserResponse> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/status")
    @Operation(summary = "Update account status", description = "Update user account status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateAccountStatus(
            @PathVariable UUID userId,
            @RequestParam AccountStatus status) {

        userService.updateAccountStatus(userId, status);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Account status updated")
                        .build()
        );
    }

    @PostMapping("/{userId}/suspend")
    @Operation(summary = "Suspend user", description = "Suspend user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> suspendUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {

        userService.suspendUser(userId, reason);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("User suspended")
                        .build()
        );
    }

    @PostMapping("/{userId}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivate suspended account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> reactivateUser(@PathVariable UUID userId) {
        userService.reactivateUser(userId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("User reactivated")
                        .build()
        );
    }

    @PostMapping("/{userId}/ban")
    @Operation(summary = "Ban user", description = "Permanently ban user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> banUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {

        userService.banUser(userId, reason);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("User banned")
                        .build()
        );
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("User deleted")
                        .build()
        );
    }
}
```

---

## Step 9.3: Profile Controller (Stub)

Create `backend/src/main/java/com/createrapp/controller/ProfileController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.request.ProfileUpdateRequest;
import com.createrapp.dto.response.ProfileResponse;
import com.createrapp.entity.enums.RoleName;
import com.createrapp.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/profiles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profile Management", description = "User profile management endpoints")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile", description = "Retrieve user profile by user ID")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID userId) {
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Create profile", description = "Create user profile for specific role")
    public ResponseEntity<ProfileResponse> createProfile(
            @PathVariable UUID userId,
            @RequestParam RoleName roleType,
            @Valid @RequestBody ProfileUpdateRequest request) {

        ProfileResponse response = profileService.createProfile(userId, roleType, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update profile", description = "Update user profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete profile", description = "Delete user profile")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID userId) {
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Step 9.4: Role Controller (Stub)

Create `backend/src/main/java/com/createrapp/controller/RoleController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.response.ApiResponse;
import com.createrapp.entity.enums.RoleName;
import com.createrapp.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Role Management", description = "User role management endpoints")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/assign")
    @Operation(summary = "Assign role", description = "Assign role to user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> assignRole(
            @RequestParam UUID userId,
            @RequestParam RoleName roleName) {

        roleService.assignRole(userId, roleName);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Role assigned successfully")
                        .build()
        );
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Remove role", description = "Remove role from user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> removeRole(
            @RequestParam UUID userId,
            @RequestParam RoleName roleName) {

        roleService.removeRole(userId, roleName);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Role removed successfully")
                        .build()
        );
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user roles", description = "Get all roles assigned to user")
    public ResponseEntity<List<RoleName>> getUserRoles(@PathVariable UUID userId) {
        List<RoleName> roles = roleService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{userId}/has-role")
    @Operation(summary = "Check role", description = "Check if user has specific role")
    public ResponseEntity<Boolean> hasRole(
            @PathVariable UUID userId,
            @RequestParam RoleName roleName) {

        boolean hasRole = roleService.hasRole(userId, roleName);
        return ResponseEntity.ok(hasRole);
    }
}
```

---

## Step 9.5: Session Controller (Stub)

Create `backend/src/main/java/com/createrapp/controller/SessionController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.response.SessionResponse;
import com.createrapp.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Session Management", description = "User session management endpoints")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get active sessions", description = "Get all active sessions for user")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(@PathVariable UUID userId) {
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Terminate session", description = "Terminate specific session")
    public ResponseEntity<Void> terminateSession(@PathVariable UUID sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Step 9.6: KYC Controller (Stub)

Create `backend/src/main/java/com/createrapp/controller/KycController.java`:

```java
package com.createrapp.controller;

import com.createrapp.config.AppConstants;
import com.createrapp.dto.request.KycSubmissionRequest;
import com.createrapp.dto.response.ApiResponse;
import com.createrapp.dto.response.KycResponse;
import com.createrapp.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/kyc")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "KYC Management", description = "KYC document verification endpoints")
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    @Operation(summary = "Submit KYC document", description = "Submit document for verification")
    public ResponseEntity<KycResponse> submitDocument(
            @RequestParam UUID userId,
            @Valid @RequestBody KycSubmissionRequest request) {

        KycResponse response = kycService.submitDocument(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get KYC document", description = "Retrieve KYC document by ID")
    public ResponseEntity<KycResponse> getDocument(@PathVariable Long documentId) {
        KycResponse response = kycService.getDocument(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user documents", description = "Get all KYC documents for user")
    public ResponseEntity<List<KycResponse>> getUserDocuments(@PathVariable UUID userId) {
        List<KycResponse> documents = kycService.getUserDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending documents", description = "Get all pending KYC documents (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KycResponse>> getPendingDocuments() {
        List<KycResponse> documents = kycService.getPendingDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/{documentId}/approve")
    @Operation(summary = "Approve document", description = "Approve KYC document (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> approveDocument(
            @PathVariable Long documentId,
            @RequestParam UUID reviewerId) {

        kycService.approveDocument(documentId, reviewerId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Document approved")
                        .build()
        );
    }

    @PostMapping("/{documentId}/reject")
    @Operation(summary = "Reject document", description = "Reject KYC document (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> rejectDocument(
            @PathVariable Long documentId,
            @RequestParam UUID reviewerId,
            @RequestParam String reason) {

        kycService.rejectDocument(documentId, reviewerId, reason);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Document rejected")
                        .build()
        );
    }
}
```

---

## Step 9.7: Compile and Verify

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Verification Checklist

- ✅ AuthController created with all authentication endpoints
- ✅ UserController created with user management endpoints
- ✅ ProfileController created
- ✅ RoleController created
- ✅ SessionController created
- ✅ KycController created
- ✅ All controllers use `@RestController` annotation
- ✅ Proper HTTP methods used
- ✅ Request validation with `@Valid`
- ✅ Swagger annotations added
- ✅ Security annotations for protected endpoints
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed REST Controllers**  
➡️ Proceed to **[10_SECURITY_CONFIG.md](./10_SECURITY_CONFIG.md)** to configure Spring Security and JWT.
