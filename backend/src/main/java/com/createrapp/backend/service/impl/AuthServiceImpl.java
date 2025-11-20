package com.createrapp.backend.service.impl;

import com.createrapp.backend.dto.request.LoginRequest;
import com.createrapp.backend.dto.request.RefreshTokenRequest;
import com.createrapp.backend.dto.request.RegisterRequest;
import com.createrapp.backend.dto.response.AuthResponse;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.UserSession;
import com.createrapp.backend.entity.enums.AccountStatus;
import com.createrapp.backend.exception.BadRequestException;
import com.createrapp.backend.exception.DuplicateResourceException;
import com.createrapp.backend.exception.UnauthorizedException;
import com.createrapp.backend.repository.UserRepository;
import com.createrapp.backend.repository.UserSessionRepository;
import com.createrapp.backend.security.JwtTokenProvider;
import com.createrapp.backend.service.AuthService;
import com.createrapp.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;


    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // check if email already exits
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // check if phone already exists (if provided)
        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // create a new user
        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .accountStatus(AccountStatus.PENDING_ONBOARDING)
                .build();

        User savedUser = userRepository.save(user);

        // send verification email (async)
        // emailService.sendVerificationEmail(savedUser)

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUserId());

        // create session
        sessionService.createSession(savedUser.getUserId(), refreshToken, null, null, null);

        log.info("User registered successfully: {}", savedUser.getUserId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .accountStatus(savedUser.getAccountStatus())
                .message("Registered successfully")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress, String userAgent) {
        log.info("Login attempt for : {}", request.getEmailOrPhone());

        // Find user by email or phone
        User user = userRepository.findByEmailOrPhoneNumber(
                        request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check account status
        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new UnauthorizedException("Account is banned");
        }
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }

        // update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Create session (will enforce max 2 sessions)
        sessionService.createSession(user.getUserId(), refreshToken, deviceInfo, ipAddress, userAgent);

        log.info("User logged in successfully: {}", user.getUserId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .accountStatus(user.getAccountStatus())
                .message("Login successful")
                .build();
    }

    @Override
    public AuthResponse loginWithPhone(String phoneNumber, String otp, String deviceInfo, String ipAddress, String userAgent) {
        // Implementation for phone logic
        // Verify OTP, find user, generate tokens, create sessions
        throw new UnsupportedOperationException("Phone login not yet implemented");
    }

    @Override
    public void sendOtp(String phoneNumber) {
        // Implementation for sending OTP
        throw new UnsupportedOperationException("OTP sending not yet implemented");
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        // Implementation for OTP verification
        throw new UnsupportedOperationException("OTP verification not yet implemented");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Extract user ID
        UUID userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());

        // Verify session exists
        String tokenHash = sessionService.hashToken(request.getRefreshToken());
        UserSession session = sessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Session not found"));

        if (session.isExpired()) {
            sessionRepository.delete(session);
            throw new UnauthorizedException("Session expired");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        log.info("Access token refreshed for user: {}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .userId(userId)
                .message("Token refreshed successfully")
                .build();
    }

    @Override
    public void logout(UUID sessionId) {
        log.info("Logging out session: {}", sessionId);
        sessionService.deleteSession(sessionId);
    }

    @Override
    public void logoutAll(UUID userId) {
        log.info("Logging out all sessions for user: {}", userId);
        sessionService.deleteAllUserSessions(userId);
    }

    @Override
    public void verifyEmail(String token) {
        // Implementation for email verification
        throw new UnsupportedOperationException("Email verification not yet implemented");
    }

    @Override
    public void resendEmailVerification(String email) {
        // Implementation for resending verification email
        throw new UnsupportedOperationException("Resend verification not yet implemented");
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Logout all sessions for security
        logoutAll(userId);

        log.info("Password changed for user: {}", userId);
    }

    @Override
    public void requestPasswordReset(String email) {
        // Implementation for password reset request
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Implementation for password reset
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }
}
