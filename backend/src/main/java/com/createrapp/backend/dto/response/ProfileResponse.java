package com.createrapp.backend.dto.response;

import com.createrapp.backend.entity.enums.Gender;
import com.createrapp.backend.entity.enums.RoleName;
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
