package com.createrapp.backend.service;

import com.createrapp.backend.dto.response.SessionResponse;
import com.createrapp.backend.entity.UserSession;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    /**
     * Create a new user session
     * Automatically enforces max 2 sessions per user
     */
    UserSession createSession(UUID userId, String refreshToken,
                              String deviceInfo, String ipAddress, String userAgent);

    /**
     * Get all active sessions for a user
     */
    List<SessionResponse> getActiveSessions(UUID userId);

    /**
     * Delete a specific session (logout)
     */
    void deleteSession(UUID sessionId);

    /**
     * Delete all sessions for a user
     */
    void deleteAllUserSessions(UUID userId);

    /**
     * Cleanup expired sessions (scheduled task)
     */
    void cleanupExpiredSessions();

    /**
     * Hash refresh token for storage
     */
    String hashToken(String token);

    /**
     * Verify if session exists and is valid
     */
    boolean isSessionValid(UUID sessionId);

    /**
     * Update session last accessed time
     */
    void updateLastAccessedTime(UUID sessionId);
}
