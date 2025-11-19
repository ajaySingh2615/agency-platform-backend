package com.createrapp.backend.service;

import com.createrapp.backend.dto.request.LoginRequest;
import com.createrapp.backend.dto.request.RefreshTokenRequest;
import com.createrapp.backend.dto.request.RegisterRequest;
import com.createrapp.backend.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    /**
     * Register a new user with email/password
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Login with email/password
     */
    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress, String userAgent);

    /**
     * Login with phone number and OTP
     */
    AuthResponse loginWithPhone(String phoneNumber, String otp, String deviceInfo, String ipAddress, String userAgent);

    /**
     * Send OTP to phone number
     */
    void sentOtp(String phoneNumber);

    /**
     * Verify OTP
     */
    boolean verifyOtp(String phoneNumber, String otp);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user (invalidate session)
     */
    void logout(UUID sessionId);

    /**
     * Logout from all devices
     */
    void logoutAll(UUID userId);

    /**
     * Verify email with token
     */
    void verifyEmail(String token);

    /**
     * Resend email verification
     */
    void resendEmailVerification(String email);

    /**
     * Change password
     */
    void changePassword(UUID userId, String oldPassword, String newPassword);

    /**
     * Reset password request (send email)
     */
    void requestPasswordReset(String email);

    /**
     * Reset password with token
     */
    void resetPassword(String token, String newPassword);
}
