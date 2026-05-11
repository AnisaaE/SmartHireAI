package com.smart_hire.ai_analysis.service;

public interface AnalysisRepository {

    AnalysisResult save(AnalysisResult analysisResult);

    AnalysisResult findById(String analysisId);

    AnalysisResult findByJobId(String jobId);

    void saveCommand(String analysisId, StartAnalysisCommand command);

    StartAnalysisCommand findCommandById(String analysisId);

    void deleteById(String analysisId);
}
