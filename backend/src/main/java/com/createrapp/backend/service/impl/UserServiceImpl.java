package com.createrapp.backend.service.impl;

import com.createrapp.backend.dto.response.UserResponse;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.enums.AccountStatus;
import com.createrapp.backend.entity.enums.RoleName;
import com.createrapp.backend.exception.BadRequestException;
import com.createrapp.backend.exception.ResourceNotFoundException;
import com.createrapp.backend.repository.UserRepository;
import com.createrapp.backend.repository.UserRoleRepository;
import com.createrapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return mapToResponse(findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(UUID userId) {
        return findUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phoneNumber));
    }

    @Override
    @Transactional
    public void updateAccountStatus(UUID userId, AccountStatus status) {
        User user = findUser(userId);
        user.setAccountStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void suspendUser(UUID userId, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new BadRequestException("Suspension reason is required");
        }
        updateAccountStatus(userId, AccountStatus.SUSPENDED);
    }

    @Override
    @Transactional
    public void reactivateUser(UUID userId) {
        updateAccountStatus(userId, AccountStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void banUser(UUID userId, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new BadRequestException("Ban reason is required");
        }
        updateAccountStatus(userId, AccountStatus.BANNED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String searchTerm) {
        if (StringUtils.isBlank(searchTerm)) {
            return getAllUsers();
        }

        String term = searchTerm.trim().toLowerCase();
        return userRepository.findAll()
                .stream()
                .filter(user -> matchesSearch(user, term))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(User user, String term) {
        return (user.getEmail() != null && user.getEmail().toLowerCase().contains(term)) ||
                (user.getPhoneNumber() != null && user.getPhoneNumber().toLowerCase().contains(term)) ||
                userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .stream()
                        .map(RoleName::name)
                        .anyMatch(role -> role.toLowerCase().contains(term));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserResponse mapToResponse(User user) {
        Set<String> roles = userRoleRepository.findRoleNamesByUserId(user.getUserId())
                .stream()
                .map(RoleName::name)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isEmailVerified(Boolean.TRUE.equals(user.getIsEmailVerified()))
                .isPhoneVerified(Boolean.TRUE.equals(user.getIsPhoneVerified()))
                .accountStatus(user.getAccountStatus())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
