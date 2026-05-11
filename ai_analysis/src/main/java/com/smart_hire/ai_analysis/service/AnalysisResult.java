package com.smart_hire.ai_analysis.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AnalysisResult(
        String analysisId,
        String jobId,
        List<Long> applicationIds,
        Map<Long, Double> applicationScores,
        Map<Long, String> applicationReasoning,
        String status,
        String summary,
        Instant createdAt,
        Instant updatedAt
) {

    public List<CandidateAnalysis> toCandidateAnalyses() {
        return applicationIds.stream()
                .map(applicationId -> new CandidateAnalysis(
                        applicationId,
                        null,
                        applicationScores.getOrDefault(applicationId, 0.0),
                        applicationReasoning.get(applicationId)
                ))
                .toList();
    }
}
