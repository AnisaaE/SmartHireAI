package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisConfiguration;

import java.util.List;
import java.util.Map;

public record AnalysisConfigurationRequest(
        Map<String, Double> scoringWeights,
        List<String> evaluationCriteria
) {

    public AnalysisConfiguration toConfiguration() {
        return new AnalysisConfiguration(scoringWeights, evaluationCriteria);
    }
}
