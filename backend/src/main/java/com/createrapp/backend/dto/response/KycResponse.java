package com.createrapp.backend.dto.response;

import com.createrapp.backend.entity.enums.DocumentType;
import com.createrapp.backend.entity.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {

    private Long documentId;
    private UUID userId;
    private DocumentType documentType;
    private String documentUrl;
    private KycStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime verifiedAt;
    private UUID reviewerId;
}
