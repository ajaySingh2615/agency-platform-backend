package com.createrapp.backend.repository;

import com.createrapp.backend.entity.ProfileBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileBrandRepository extends JpaRepository<ProfileBrand, Long> {
    // Find by user ID
    Optional<ProfileBrand> findByUser_UserId(UUID userId);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find by brand name
    List<ProfileBrand> findByBrandNameContainingIgnoreCase(String brandName);

    // Find by industry
    List<ProfileBrand> findByIndustry(String industry);

    // Find verified brands
    List<ProfileBrand> findByIsVerifiedTrue();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
