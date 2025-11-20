package com.createrapp.backend.service.impl;

import com.createrapp.backend.dto.request.KycSubmissionRequest;
import com.createrapp.backend.dto.response.KycResponse;
import com.createrapp.backend.entity.KycDocument;
import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.enums.KycStatus;
import com.createrapp.backend.exception.BadRequestException;
import com.createrapp.backend.exception.ResourceNotFoundException;
import com.createrapp.backend.repository.KycDocumentRepository;
import com.createrapp.backend.repository.UserRepository;
import com.createrapp.backend.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public KycResponse submitDocument(UUID userId, KycSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        KycDocument document = kycDocumentRepository
                .findByUser_UserIdAndDocumentType(userId, request.getDocumentType())
                .orElse(KycDocument.builder().user(user).documentType(request.getDocumentType()).build());

        document.setDocumentUrl(request.getDocumentUrl());
        document.setStatus(KycStatus.PENDING);
        document.setRejectionReason(null);
        document.setReviewedAt(null);
        document.setVerifiedAt(null);
        document.setReviewer(null);

        KycDocument saved = kycDocumentRepository.save(document);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KycResponse getDocument(Long documentId) {
        return kycDocumentRepository.findById(documentId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found with id: " + documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycResponse> getUserDocuments(UUID userId) {
        return kycDocumentRepository.findByUser_UserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveDocument(Long documentId, UUID reviewerId) {
        KycDocument document = getDocumentEntity(documentId);
        User reviewer = getReviewer(reviewerId);

        document.setStatus(KycStatus.APPROVED);
        document.setReviewer(reviewer);
        document.setVerifiedAt(LocalDateTime.now());
        document.setReviewedAt(LocalDateTime.now());
        document.setRejectionReason(null);

        kycDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public void rejectDocument(Long documentId, UUID reviewerId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Rejection reason is required");
        }

        KycDocument document = getDocumentEntity(documentId);
        User reviewer = getReviewer(reviewerId);

        document.setStatus(KycStatus.REJECTED);
        document.setReviewer(reviewer);
        document.setReviewedAt(LocalDateTime.now());
        document.setVerifiedAt(null);
        document.setRejectionReason(reason);

        kycDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycResponse> getPendingDocuments() {
        return kycDocumentRepository.findByStatusOrderBySubmittedAtAsc(KycStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private KycDocument getDocumentEntity(Long documentId) {
        return kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found with id: " + documentId));
    }

    private User getReviewer(UUID reviewerId) {
        if (reviewerId == null) {
            return null;
        }
        return userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + reviewerId));
    }

    private KycResponse mapToResponse(KycDocument document) {
        return KycResponse.builder()
                .documentId(document.getDocumentId())
                .userId(document.getUser().getUserId())
                .documentType(document.getDocumentType())
                .documentUrl(document.getDocumentUrl())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .submittedAt(document.getSubmittedAt())
                .reviewedAt(document.getReviewedAt())
                .verifiedAt(document.getVerifiedAt())
                .reviewerId(document.getReviewer() != null ? document.getReviewer().getUserId() : null)
                .build();
    }
}
