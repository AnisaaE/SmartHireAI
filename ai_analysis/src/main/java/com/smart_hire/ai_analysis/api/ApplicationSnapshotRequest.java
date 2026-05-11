package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.ApplicationSnapshot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationSnapshotRequest(
        @NotNull(message = "must not be null")
        Long applicationId,
        @NotNull(message = "must not be null")
        Long candidateId,
        @NotBlank(message = "must not be blank")
        String cvDocumentId,
        @NotBlank(message = "must not be blank")
        String candidateLabel
) {

    public ApplicationSnapshot toSnapshot() {
        return new ApplicationSnapshot(applicationId, candidateId, cvDocumentId, candidateLabel);
    }
}
