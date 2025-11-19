package com.createrapp.backend.repository;

import com.createrapp.backend.entity.SocialIdentity;
import com.createrapp.backend.entity.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialIdentityRepository extends JpaRepository<SocialIdentity, Long> {

    // find by provider and provider user ID
    Optional<SocialIdentity> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    // find all social identities for a user
    List<SocialIdentity> findByUser_UserId(UUID userId);

    // find by user and provider
    Optional<SocialIdentity> findByUser_UserIdAndProvider(UUID userId, SocialProvider provider);

    // Check if provider identity exists
    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    // find by email
    Optional<SocialIdentity> findByEmail(String email);

    // Delete all by user
    void deleteByUser_UserId(UUID userId);
}
