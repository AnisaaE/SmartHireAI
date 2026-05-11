package com.smart_hire.ai_analysis.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateAnalysisStatusRequest(@NotBlank(message = "must not be blank") String status) {
}
