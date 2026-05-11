package com.smart_hire.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(
        @NotNull Long jobId,
        @NotNull Long candidateId,
        @NotBlank String cvDocumentId
) {
}
