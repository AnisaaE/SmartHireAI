package com.smart_hire.ai_analysis.service;

import java.util.List;
import java.util.Map;

public record AnalysisConfiguration(
        Map<String, Double> scoringWeights,
        List<String> evaluationCriteria
) {
}
