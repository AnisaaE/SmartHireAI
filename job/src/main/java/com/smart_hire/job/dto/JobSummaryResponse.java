package com.smart_hire.job.dto;

public record JobSummaryResponse(
        Long id,
        Long recruiterId,
        String title,
        String status
) {
}
