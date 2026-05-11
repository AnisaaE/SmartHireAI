package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.ApplicationSnapshot;

public record ApplicationSnapshotRequest(
        Long applicationId,
        Long candidateId,
        String cvDocumentId,
        String candidateLabel
) {

    public ApplicationSnapshot toSnapshot() {
        return new ApplicationSnapshot(applicationId, candidateId, cvDocumentId, candidateLabel);
    }
}
