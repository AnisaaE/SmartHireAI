package com.smart_hire.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationRequest(
        @NotBlank String cvDocumentId
) {
}
