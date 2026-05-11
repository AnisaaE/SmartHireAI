package com.smart_hire.application.dto;

public record ApplicationSummaryResponse(
        Long id,
        Long jobId,
        Long candidateId,
        String status
) {
}
