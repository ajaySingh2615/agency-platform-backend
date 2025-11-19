package com.createrapp.backend.repository;

import com.createrapp.backend.entity.ProfileGifter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileGifterRepository extends JpaRepository<ProfileGifter, Long> {

    // Find by user ID
    Optional<ProfileGifter> findByUser_UserId(UUID userId);

    // Check if profile exists for user
    boolean existsByUser_UserId(UUID userId);

    // Find VIP gifters
    List<ProfileGifter> findByVipStatusTrue();

    // Find by level
    List<ProfileGifter> findByLevel(Integer level);

    // Find gifters by level range
    List<ProfileGifter> findByLevelBetween(Integer minLevel, Integer maxLevel);

    // Update total spent
    @Modifying
    @Query("UPDATE ProfileGifter pg SET pg.totalSpent = pg.totalSpent + :amount WHERE pg.user.userId = :userId")
    int updateTotalSpent(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    // Update level
    @Modifying
    @Query("UPDATE ProfileGifter pg SET pg.level = :level WHERE pg.user.userId = :userId")
    int updateLevel(@Param("userId") UUID userId, @Param("level") Integer level);

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
