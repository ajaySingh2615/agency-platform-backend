package com.createrapp.backend.controller;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.response.ApiResponse;
import com.createrapp.backend.dto.response.UserResponse;
import com.createrapp.backend.entity.enums.AccountStatus;
import com.createrapp.backend.service.UserService;
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
