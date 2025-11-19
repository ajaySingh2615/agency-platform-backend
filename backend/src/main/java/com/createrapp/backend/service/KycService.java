package com.createrapp.backend.service;

import com.createrapp.backend.dto.request.KycSubmissionRequest;
import com.createrapp.backend.dto.response.KycResponse;

import java.util.List;
import java.util.UUID;

public interface KycService {

    KycResponse submitDocument(UUID userId, KycSubmissionRequest request);

    KycResponse getDocument(Long documentId);

    List<KycResponse> getUserDocuments(UUID userId);

    void approveDocument(Long documentId, UUID reviewerId);

    void rejectDocument(Long documentId, UUID reviewerId, String reason);

    List<KycResponse> getPendingDocuments();
}
