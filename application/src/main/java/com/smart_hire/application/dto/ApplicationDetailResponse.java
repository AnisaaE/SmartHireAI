package com.smart_hire.application.dto;

public record ApplicationDetailResponse(
        Long id,
        Long jobId,
        Long candidateId,
        String cvDocumentId,
        String status
) {
}
