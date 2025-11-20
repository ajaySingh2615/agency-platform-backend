package com.createrapp.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.response.ApiResponse;
import com.createrapp.backend.entity.enums.RoleName;
import com.createrapp.backend.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
                        .build());
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
                        .build());
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
