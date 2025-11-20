package com.createrapp.backend.controller;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.response.SessionResponse;
import com.createrapp.backend.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Session Management", description = "User session management endpoints")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get active sessions", description = "Get all active sessions for user")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(@PathVariable UUID userId) {
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Terminate session", description = "Terminate specific session")
    public ResponseEntity<Void> terminateSession(@PathVariable UUID sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
