package com.createrapp.backend.repository;

import com.createrapp.backend.entity.ProfileHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileHostRepository extends JpaRepository<ProfileHost, Long> {

    // find by user ID
    Optional<ProfileHost> findByUser_UserId(UUID userId);

    // check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find by display name (for search)
    List<ProfileHost> findByDisplayNameContainingIgnoreCase(String displayName);

    // Find verified hosts
    List<ProfileHost> findByIsVerifiedTrue();

    // Find by onboarding step
    List<ProfileHost> findByOnboardingStep(Integer step);

    // Find incomplete onboarding
    @Query("SELECT ph FROM ProfileHost ph WHERE ph.onboardingStep < 5")
    List<ProfileHost> findIncompleteOnboarding();

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
