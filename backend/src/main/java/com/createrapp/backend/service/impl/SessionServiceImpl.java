package com.createrapp.backend.service.impl;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.response.SessionResponse;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.UserSession;
import com.createrapp.backend.exception.ResourceNotFoundException;
import com.createrapp.backend.repository.UserRepository;
import com.createrapp.backend.repository.UserSessionRepository;
import com.createrapp.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpiration;

    @Override
    public UserSession createSession(UUID userId, String refreshToken, String deviceInfo, String ipAddress, String userAgent) {
        log.info("Creating session for user: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check active session count
        long activeCount = sessionRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());

        if (activeCount >= AppConstants.MAX_SESSIONS_PER_USER) {
            // Delete oldest session
            List<UserSession> oldestSessions = sessionRepository
                    .findOldestActiveSession(userId, LocalDateTime.now());
            if (!oldestSessions.isEmpty()) {
                sessionRepository.delete(oldestSessions.get(0));
                log.info("Deleted oldest session to make room for new session");
            }
        }

        // Hash refresh token
        String tokenHash = hashToken(refreshToken);

        // Create new session
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(tokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        UserSession savedSession = sessionRepository.save(session);
        log.info("Session created: {}", savedSession.getSessionId());

        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveSessions(UUID userId) {
        List<UserSession> sessions = sessionRepository
                .findActiveSessionsByUserId(userId, LocalDateTime.now());
        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(UUID sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Session deleted: {}", sessionId);
    }

    @Override
    public void deleteAllUserSessions(UUID userId) {
        sessionRepository.deleteByUser_UserId(userId);
        log.info("All sessions delete for user: {}", userId);
    }

    @Override
    @Scheduled(cron = "${app.session.cleanup-cron:0 0 */6 * * *}")
    public void cleanupExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up {} expired sessions", deleted);
    }

    @Override
    public String hashToken(String token) {
        return passwordEncoder.encode(token);
    }

    @Override
    public boolean isSessionValid(UUID sessionId) {
        return sessionRepository.isSessionActive(sessionId, LocalDateTime.now());
    }

    @Override
    public void updateLastAccessedTime(UUID sessionId) {
        sessionRepository.updateLastAccessedAt(sessionId, LocalDateTime.now());
    }

    private SessionResponse mapToResponse(UserSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .deviceInfo(session.getDeviceInfo())
                .ipAddress(session.getIpAddress())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .build();
    }
}
