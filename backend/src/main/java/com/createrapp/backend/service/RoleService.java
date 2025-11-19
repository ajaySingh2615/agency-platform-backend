package com.createrapp.backend.service;

import com.createrapp.backend.entity.enums.RoleName;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    void assignRole(UUID userId, RoleName roleName);

    void removeRole(UUID userId, RoleName roleName);

    List<RoleName> getUserRoles(UUID userId);

    boolean hasRole(UUID userId, RoleName roleName);
}
