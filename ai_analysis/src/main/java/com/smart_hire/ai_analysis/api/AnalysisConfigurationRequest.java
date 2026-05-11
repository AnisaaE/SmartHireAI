package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisConfiguration;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record AnalysisConfigurationRequest(
        @NotEmpty(message = "must not be empty")
        Map<String, Double> scoringWeights,
        @NotEmpty(message = "must not be empty")
        List<String> evaluationCriteria
) {

    public AnalysisConfiguration toConfiguration() {
        return new AnalysisConfiguration(scoringWeights, evaluationCriteria);
    }
}
