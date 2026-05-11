package com.smart_hire.ai_analysis.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAnalysisRepository implements AnalysisRepository {

    private final Map<String, AnalysisResult> storage = new ConcurrentHashMap<>();

    @Override
    public AnalysisResult save(AnalysisResult analysisResult) {
        storage.put(analysisResult.analysisId(), analysisResult);
        return analysisResult;
    }

    @Override
    public AnalysisResult findById(String analysisId) {
        return storage.get(analysisId);
    }

    @Override
    public AnalysisResult findByJobId(String jobId) {
        return storage.values().stream()
                .filter(result -> result.jobId().equals(jobId))
                .findFirst()
                .orElse(null);
    }
}
