package com.createrapp.backend.service;

import com.createrapp.backend.dto.response.UserResponse;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.enums.AccountStatus;

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
