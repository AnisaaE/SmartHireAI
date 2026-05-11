package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.UpdateAnalysisCommand;

public record UpdateAnalysisRequest(
        java.util.Map<String, Double> scoringWeights,
        java.util.List<String> evaluationCriteria
) {

    public UpdateAnalysisCommand toCommand() {
        return new UpdateAnalysisCommand(
                new com.smart_hire.ai_analysis.service.AnalysisConfiguration(scoringWeights, evaluationCriteria)
        );
    }
}
