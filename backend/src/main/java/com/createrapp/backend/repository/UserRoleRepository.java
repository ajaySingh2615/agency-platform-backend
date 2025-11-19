package com.createrapp.backend.repository;

import com.createrapp.backend.entity.UserRole;
import com.createrapp.backend.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    // find all roles for a user
    List<UserRole> findByUser_UserId(UUID userId);

    // find all users with a specific role
    List<UserRole> findByRole_RoleName(RoleName roleName);

    // check if user has a specific role
    boolean existsByUser_UserIdAndRole_RoleName(UUID userId, RoleName roleName);

    // Delete user role
    void deleteByUser_UserIdAndRole_RoleId(UUID userId, Integer roleId);

    // Count roles per user
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // Get role names for a user
    @Query("SELECT ur.role.roleName FROM UserRole ur WHERE ur.user.userId = :userId")
    List<RoleName> findRoleNamesByUserId(@Param("userId") UUID userId);

}
