package com.createrapp.backend.repository;

import com.createrapp.backend.entity.KycDocument;
import com.createrapp.backend.entity.enums.DocumentType;
import com.createrapp.backend.entity.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    // Find all documents for a user
    List<KycDocument> findByUser_UserId(UUID userId);

    // Find by user and document type
    Optional<KycDocument> findByUser_UserIdAndDocumentType(UUID userId, DocumentType documentType);

    // Find by status
    List<KycDocument> findByStatus(KycStatus status);

    // Find pending documents
    List<KycDocument> findByStatusOrderBySubmittedAtAsc(KycStatus status);

    // Find documents by user and status
    List<KycDocument> findByUser_UserIdAndStatus(UUID userId, KycStatus status);

    // Check if user has approved document
    boolean existsByUser_UserIdAndStatus(UUID userId, KycStatus status);

    // Count documents by user
    long countByUser_UserId(UUID userId);

    // Approve document
    @Modifying
    @Query("UPDATE KycDocument k SET k.status = 'APPROVED', k.verifiedAt = :verifiedAt, " +
            "k.reviewer.userId = :reviewerId WHERE k.documentId = :documentId")
    int approveDocument(
            @Param("documentId") Long documentId,
            @Param("reviewerId") UUID reviewerId,
            @Param("verifiedAt") LocalDateTime verifiedAt
    );

    // Reject document
    @Modifying
    @Query("UPDATE KycDocument k SET k.status = 'REJECTED', k.rejectionReason = :reason, " +
            "k.reviewedAt = :reviewedAt, k.reviewer.userId = :reviewerId WHERE k.documentId = :documentId")
    int rejectDocument(
            @Param("documentId") Long documentId,
            @Param("reviewerId") UUID reviewerId,
            @Param("reason") String reason,
            @Param("reviewedAt") LocalDateTime reviewedAt
    );

    // Find documents submitted after date
    List<KycDocument> findBySubmittedAtAfter(LocalDateTime date);

    // Delete by user ID
    void deleteByUser_UserId(UUID userId);
}
