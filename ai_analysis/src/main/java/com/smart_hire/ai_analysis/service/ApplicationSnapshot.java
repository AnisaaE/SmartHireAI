package com.smart_hire.ai_analysis.service;

public record ApplicationSnapshot(
        Long applicationId,
        Long candidateId,
        String cvDocumentId,
        String candidateLabel
) {
}
