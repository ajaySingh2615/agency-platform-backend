# Step 10A: Data Transfer Objects (DTOs)

## Objective

Create DTO classes for request and response objects to transfer data between client and server.

---

## DTO Best Practices

1. **Separate Request and Response DTOs**
2. Use validation annotations (`@NotNull`, `@Email`, etc.)
3. Use Lombok to reduce boilerplate
4. Keep DTOs immutable where possible
5. Don't expose entity objects directly

---

## Step 10A.1: Request DTOs

### RegisterRequest

Create `backend/src/main/java/com/createrapp/dto/request/RegisterRequest.java`:

```java
package com.createrapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
            message = "Password must contain at least one uppercase, lowercase, digit, and special character")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
```

### LoginRequest

Create `backend/src/main/java/com/createrapp/dto/request/LoginRequest.java`:

```java
package com.createrapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or phone number is required")
    private String emailOrPhone;

    @NotBlank(message = "Password is required")
    private String password;
}
```

### RefreshTokenRequest

Create `backend/src/main/java/com/createrapp/dto/request/RefreshTokenRequest.java`:

```java
package com.createrapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
```

### ProfileUpdateRequest

Create `backend/src/main/java/com/createrapp/dto/request/ProfileUpdateRequest.java`:

```java
package com.createrapp.dto.request;

import com.createrapp.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    // Common fields
    @NotBlank(message = "Display name is required")
    private String displayName;

    // Host-specific fields
    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    private String bio;
    private String profilePicUrl;

    // Agency-specific fields
    private String companyName;
    private String registrationNumber;
    private String contactPerson;

    // Brand-specific fields
    private String brandName;
    private String websiteUrl;
    private String industry;
}
```

### KycSubmissionRequest

Create `backend/src/main/java/com/createrapp/dto/request/KycSubmissionRequest.java`:

```java
package com.createrapp.dto.request;

import com.createrapp.entity.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycSubmissionRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}
```

### SocialLoginRequest

Create `backend/src/main/java/com/createrapp/dto/request/SocialLoginRequest.java`:

```java
package com.createrapp.dto.request;

import com.createrapp.entity.enums.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    @NotNull(message = "Provider is required")
    private SocialProvider provider;

    @NotBlank(message = "ID token is required")
    private String idToken;
}
```

### PhoneLoginRequest

Create `backend/src/main/java/com/createrapp/dto/request/PhoneLoginRequest.java`:

```java
package com.createrapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLoginRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
}
```

### VerifyOtpRequest

Create `backend/src/main/java/com/createrapp/dto/request/VerifyOtpRequest.java`:

```java
package com.createrapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
}
```

### RoleSelectionRequest

Create `backend/src/main/java/com/createrapp/dto/request/RoleSelectionRequest.java`:

```java
package com.createrapp.dto.request;

import com.createrapp.entity.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSelectionRequest {

    @NotNull(message = "Role is required")
    private RoleName roleName;
}
```

---

## Step 10A.2: Response DTOs

### AuthResponse

Create `backend/src/main/java/com/createrapp/dto/response/AuthResponse.java`:

```java
package com.createrapp.dto.response;

import com.createrapp.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String phoneNumber;
    private AccountStatus accountStatus;
    private String message;
}
```

### UserResponse

Create `backend/src/main/java/com/createrapp/dto/response/UserResponse.java`:

```java
package com.createrapp.dto.response;

import com.createrapp.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private AccountStatus accountStatus;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

### ProfileResponse

Create `backend/src/main/java/com/createrapp/dto/response/ProfileResponse.java`:

```java
package com.createrapp.dto.response;

import com.createrapp.entity.enums.Gender;
import com.createrapp.entity.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long profileId;
    private UUID userId;
    private RoleName roleType;

    // Host fields
    private String displayName;
    private Gender gender;
    private LocalDate dob;
    private String bio;
    private String profilePicUrl;
    private Integer onboardingStep;

    // Agency fields
    private String companyName;
    private String registrationNumber;
    private String contactPerson;
    private String agencyCode;

    // Brand fields
    private String brandName;
    private String websiteUrl;
    private String industry;

    // Gifter fields
    private Integer level;
    private Boolean vipStatus;
    private BigDecimal totalSpent;

    // Common fields
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### KycResponse

Create `backend/src/main/java/com/createrapp/dto/response/KycResponse.java`:

```java
package com.createrapp.dto.response;

import com.createrapp.entity.enums.DocumentType;
import com.createrapp.entity.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {

    private Long documentId;
    private UUID userId;
    private DocumentType documentType;
    private String documentUrl;
    private KycStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime verifiedAt;
    private UUID reviewerId;
}
```

### SessionResponse

Create `backend/src/main/java/com/createrapp/dto/response/SessionResponse.java`:

```java
package com.createrapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private UUID sessionId;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessedAt;
    private boolean isCurrentSession;
}
```

### ApiResponse

Create `backend/src/main/java/com/createrapp/dto/response/ApiResponse.java`:

```java
package com.createrapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
```

### ErrorResponse

Create `backend/src/main/java/com/createrapp/dto/response/ErrorResponse.java`:

```java
package com.createrapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

---

## Step 10A.3: Compile and Verify

```bash
# Navigate to backend folder
cd backend

# Clean and compile
mvn clean compile

# Expected output: BUILD SUCCESS
```

---

## Verification Checklist

- ✅ All request DTOs created with validation annotations
- ✅ All response DTOs created
- ✅ Lombok annotations used (`@Data`, `@Builder`)
- ✅ Validation constraints added where appropriate
- ✅ ApiResponse and ErrorResponse for generic responses
- ✅ Project compiles without errors

---

## Next Step

✅ **Completed DTOs**  
➡️ Proceed to **[11_EXCEPTION_HANDLING.md](./11_EXCEPTION_HANDLING.md)** to implement exception handling.
