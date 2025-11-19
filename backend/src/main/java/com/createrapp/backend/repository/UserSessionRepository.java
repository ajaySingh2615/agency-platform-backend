package com.createrapp.backend.repository;

import com.createrapp.backend.entity.UserSession;
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
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    // Find by refresh token hash
    Optional<UserSession> findByRefreshTokenHash(String tokenHash);

    // Find all sessions for a user
    List<UserSession> findByUser_UserId(UUID userId);

    // Find active sessions for a user (not expired)
    @Query("SELECT s FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    // Count active sessions for a user
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Find oldest session for a user
    @Query("SELECT s FROM UserSession s WHERE s.user.userId = :userId AND s.expiresAt > :now ORDER BY s.createdAt ASC")
    List<UserSession> findOldestActiveSession(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Delete expired sessions
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    // Delete all sessions for a user
    @Modifying
    void deleteByUser_UserId(UUID userId);

    // Update last accessed time
    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessedAt = :time WHERE s.sessionId = :sessionId")
    int updateLastAccessedAt(@Param("sessionId") UUID sessionId, @Param("time") LocalDateTime time);

    // Check if session exists and is active
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM UserSession s " +
            "WHERE s.sessionId = :sessionId AND s.expiresAt > :now")
    boolean isSessionActive(@Param("sessionId") UUID sessionId, @Param("now") LocalDateTime now);
}
