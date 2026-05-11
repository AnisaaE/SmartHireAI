package com.smart_hire.ai_analysis.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAnalysisRepository implements AnalysisRepository {

    private final Map<String, AnalysisResult> storage = new ConcurrentHashMap<>();
    private final Map<String, StartAnalysisCommand> commands = new ConcurrentHashMap<>();

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
                .sorted((left, right) -> right.updatedAt().compareTo(left.updatedAt()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void saveCommand(String analysisId, StartAnalysisCommand command) {
        commands.put(analysisId, command);
    }

    @Override
    public StartAnalysisCommand findCommandById(String analysisId) {
        return commands.get(analysisId);
    }

    @Override
    public void deleteById(String analysisId) {
        storage.remove(analysisId);
        commands.remove(analysisId);
    }
}
