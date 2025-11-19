package com.createrapp.backend.repository;

import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // find by email
    Optional<User> findByEmail(String email);

    // find by phone number
    Optional<User> findByPhoneNumber(String phoneNumber);

    // find by email and phone number
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    // check if email exits
    boolean existsByEmail(String email);

    // check if phone number exists
    boolean exitsByPhoneNumber(String phoneNumber);

    // find by account status
    List<User> findByAccountStatus(AccountStatus status);

    // find users created after a certain date
    List<User> findByCreatedAtAfter(LocalDateTime data);

    // find verified email users
    List<User> findByIsEmailVerifiedTrue();

    // find verified phone users
    List<User> findByIsPhoneVerifiedTrue();

    // Custom query: update last login timestamp
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    int updateLastLoginAt(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    // Custom query: Update account status
    @Modifying
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.userId = :userId")
    int updateAccountStatus(@Param("userId") UUID userId, @Param("status") AccountStatus status);

    // Custom query: Verify email
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true WHERE u.userId = :userId")
    int verifyEmail(@Param("userId") UUID userId);

    // Custom query: Verify phone
    @Modifying
    @Query("UPDATE User u SET u.isPhoneVerified = true WHERE u.userId = :userId")
    int verifyPhone(@Param("userId") UUID userId);

    // Find users with specific role
    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur WHERE ur.role.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") com.createrapp.entity.enums.RoleName roleName);
}
