package com.smart_hire.ai_analysis.service;

public interface AnalysisService {

    AnalysisResult startAnalysis(StartAnalysisCommand command);

    AnalysisResult getAnalysis(String analysisId);
}
