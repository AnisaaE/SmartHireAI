package com.smart_hire.ai_analysis.service;

public interface AnalysisService {

    AnalysisResult startAnalysis(StartAnalysisCommand command);

    AnalysisResult getAnalysis(String analysisId);

    AnalysisResult getReport(String jobId);

    java.util.List<CandidateAnalysis> getCandidates(String analysisId);
}
