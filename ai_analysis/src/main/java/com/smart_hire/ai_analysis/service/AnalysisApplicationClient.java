package com.smart_hire.ai_analysis.service;

public interface AnalysisApplicationClient {

    ApplicationDetail getApplication(Long applicationId);

    record ApplicationDetail(
            Long id,
            Long jobId,
            Long candidateId,
            String cvDocumentId,
            String status
    ) {
    }
}
