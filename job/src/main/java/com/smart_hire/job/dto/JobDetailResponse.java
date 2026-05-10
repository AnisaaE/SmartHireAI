package com.smart_hire.job.dto;

public record JobDetailResponse(
        Long id,
        Long recruiterId,
        String title,
        String description,
        String location,
        String employmentType,
        String status
) {
}
