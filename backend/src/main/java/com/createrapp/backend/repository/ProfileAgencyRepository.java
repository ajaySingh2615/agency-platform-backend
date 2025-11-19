package com.createrapp.backend.repository;

import com.createrapp.backend.entity.ProfileAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileAgencyRepository extends JpaRepository<ProfileAgency, Long> {

    // Find by user ID
    Optional<ProfileAgency> findByUser_UserId(UUID userId);

    // Find by agency code
    Optional<ProfileAgency> findByAgencyCode(String agencyCode);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Check if agency code exists
    boolean existsByAgencyCode(String agencyCode);

    // Find by company name
    List<ProfileAgency> findByCompanyNameContainingIgnoreCase(String companyName);

    // Find verified agencies
    List<ProfileAgency> findByIsVerifiedTrue();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
