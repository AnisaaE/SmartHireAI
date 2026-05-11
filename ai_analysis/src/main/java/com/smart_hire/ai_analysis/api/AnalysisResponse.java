package com.smart_hire.ai_analysis.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AnalysisResponse(
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
}
