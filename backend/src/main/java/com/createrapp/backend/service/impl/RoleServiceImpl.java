package com.createrapp.backend.service.impl;

import com.createrapp.backend.entity.Role;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.UserRole;
import com.createrapp.backend.entity.enums.RoleName;
import com.createrapp.backend.exception.BadRequestException;
import com.createrapp.backend.exception.ResourceNotFoundException;
import com.createrapp.backend.repository.RoleRepository;
import com.createrapp.backend.repository.UserRepository;
import com.createrapp.backend.repository.UserRoleRepository;
import com.createrapp.backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public void assignRole(UUID userId, RoleName roleName) {
        User user = findUser(userId);
        Role role = findRole(roleName);

        if (userRoleRepository.existsByUser_UserIdAndRole_RoleName(userId, roleName)) {
            throw new BadRequestException("User already has role: " + roleName);
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .assignedAt(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);
    }

    @Override
    @Transactional
    public void removeRole(UUID userId, RoleName roleName) {
        Role role = findRole(roleName);
        if (!userRoleRepository.existsByUser_UserIdAndRole_RoleName(userId, roleName)) {
            throw new ResourceNotFoundException("Role not assigned to user");
        }
        userRoleRepository.deleteByUser_UserIdAndRole_RoleId(userId, role.getRoleId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleName> getUserRoles(UUID userId) {
        return userRoleRepository.findRoleNamesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, RoleName roleName) {
        return userRoleRepository.existsByUser_UserIdAndRole_RoleName(userId, roleName);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Role findRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }
}
