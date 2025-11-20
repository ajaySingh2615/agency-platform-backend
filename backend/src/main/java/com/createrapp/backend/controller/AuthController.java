package com.createrapp.backend.controller;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.request.LoginRequest;
import com.createrapp.backend.dto.request.RefreshTokenRequest;
import com.createrapp.backend.dto.request.RegisterRequest;
import com.createrapp.backend.dto.response.ApiResponse;
import com.createrapp.backend.dto.response.AuthResponse;
import com.createrapp.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register with email/phone and password")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email/phone and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String deviceInfo = extractDeviceInfo(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, deviceInfo, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout from current session")
    public ResponseEntity<ApiResponse> logout(@RequestParam UUID sessionId) {
        authService.logout(sessionId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Logged out successfully")
                        .build()
        );
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Invalidate all user sessions")
    public ResponseEntity<ApiResponse> logoutAll(
            @RequestParam UUID userId) {

        authService.logoutAll(userId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Logged out from all devices")
                        .build()
        );
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP", description = "Send OTP to phone number")
    public ResponseEntity<ApiResponse> sendOtp(
            @RequestParam String phoneNumber) {

        authService.sendOtp(phoneNumber);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .build()
        );
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email with token")
    public ResponseEntity<ApiResponse> verifyEmail(
            @RequestParam String token) {

        authService.verifyEmail(token);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Email verified successfully")
                        .build()
        );
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    public ResponseEntity<ApiResponse> resendVerification(
            @RequestParam String email) {

        authService.resendEmailVerification(email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Verification email sent")
                        .build()
        );
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestParam UUID userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestParam String email) {

        authService.requestPasswordReset(email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset email sent")
                        .build()
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset successful")
                        .build()
        );
    }

    // Helper methods
    private String extractDeviceInfo(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        // Parse user agent to extract device info (simplified)
        if(userAgent != null){
            if(userAgent.contains("Android")){
                return "Android Device";
            } else if(userAgent.contains("iPhone") || userAgent.contains("iPad")){
                return "ios Device";
            } else{
                return "Web Browser";
            }
        }
        return "Unknown Device";
    }

    private String extractIpAddress(HttpServletRequest request){
        String ipAddress = request.getHeader("X-Forwarded-For");
        if(ipAddress == null || ipAddress.isEmpty()){
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


}
