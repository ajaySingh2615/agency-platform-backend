package com.createrapp.backend.dto.response;

import com.createrapp.backend.entity.enums.AccountStatus;
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
