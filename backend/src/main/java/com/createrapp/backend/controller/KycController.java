package com.createrapp.backend.controller;

import com.createrapp.backend.config.AppConstants;
import com.createrapp.backend.dto.request.KycSubmissionRequest;
import com.createrapp.backend.dto.response.ApiResponse;
import com.createrapp.backend.dto.response.KycResponse;
import com.createrapp.backend.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_VERSION_V1 + "/kyc")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "KYC Management", description = "KYC document verification endpoints")
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    @Operation(summary = "Submit KYC document", description = "Submit document for verification")
    public ResponseEntity<KycResponse> submitDocument(
            @RequestParam UUID userId,
            @Valid @RequestBody KycSubmissionRequest request) {

        KycResponse response = kycService.submitDocument(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get KYC document", description = "Retrieve KYC document by ID")
    public ResponseEntity<KycResponse> getDocument(@PathVariable Long documentId) {
        KycResponse response = kycService.getDocument(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user documents", description = "Get all KYC documents for user")
    public ResponseEntity<List<KycResponse>> getUserDocuments(@PathVariable UUID userId) {
        List<KycResponse> documents = kycService.getUserDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending documents", description = "Get all pending KYC documents (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KycResponse>> getPendingDocuments() {
        List<KycResponse> documents = kycService.getPendingDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/{documentId}/approve")
    @Operation(summary = "Approve document", description = "Approve KYC document (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> approveDocument(
            @PathVariable Long documentId,
            @RequestParam UUID reviewerId) {

        kycService.approveDocument(documentId, reviewerId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Document approved")
                        .build()
        );
    }

    @PostMapping("/{documentId}/reject")
    @Operation(summary = "Reject document", description = "Reject KYC document (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> rejectDocument(
            @PathVariable Long documentId,
            @RequestParam UUID reviewerId,
            @RequestParam String reason) {

        kycService.rejectDocument(documentId, reviewerId, reason);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Document rejected")
                        .build()
        );
    }
}
