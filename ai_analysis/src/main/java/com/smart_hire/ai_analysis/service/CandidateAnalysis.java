package com.smart_hire.ai_analysis.service;

public record CandidateAnalysis(
        Long applicationId,
        String cvDocumentId,
        double score,
        String reasoning
) {
}
