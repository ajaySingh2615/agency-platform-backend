package com.createrapp.backend.repository;

import com.createrapp.backend.entity.Role;
import com.createrapp.backend.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // Find by role name
    Optional<Role> findByRoleName(RoleName roleName);

    // Check if role exists
    boolean existsByRoleName(RoleName roleName);
}
