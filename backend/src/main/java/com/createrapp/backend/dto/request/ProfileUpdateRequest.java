package com.createrapp.backend.dto.request;

import com.createrapp.backend.entity.enums.Gender;
import com.createrapp.backend.validation.annotation.ValidAge;
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
    @ValidAge(minAge = 18, message = "User must be at least 18 years old")
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
