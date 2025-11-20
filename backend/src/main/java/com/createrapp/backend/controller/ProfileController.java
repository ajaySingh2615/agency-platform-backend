package com.createrapp.backend.controller;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.request.ProfileUpdateRequest;
import com.createrapp.backend.dto.response.ProfileResponse;
import com.createrapp.backend.entity.enums.RoleName;
import com.createrapp.backend.service.ProfileService;
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
