package com.smart_hire.job.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateJobStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {
}
