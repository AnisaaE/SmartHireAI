package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.UpdateAnalysisCommand;
import jakarta.validation.constraints.NotEmpty;

public record UpdateAnalysisRequest(
        @NotEmpty(message = "must not be empty")
        java.util.Map<String, Double> scoringWeights,
        @NotEmpty(message = "must not be empty")
        java.util.List<String> evaluationCriteria
) {

    public UpdateAnalysisCommand toCommand() {
        return new UpdateAnalysisCommand(
                new com.smart_hire.ai_analysis.service.AnalysisConfiguration(scoringWeights, evaluationCriteria)
        );
    }
}
