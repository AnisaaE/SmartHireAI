package com.smart_hire.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationStatusRequest(
        @NotBlank String status
) {
}
