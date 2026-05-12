package com.smart_hire.ai_analysis.service;

public interface AnalysisService {

    AnalysisResult startAnalysis(StartAnalysisCommand command);

    AnalysisResult getAnalysis(String analysisId);

    AnalysisResult getReport(String jobId);

    java.util.List<CandidateAnalysis> getCandidates(String analysisId);

    AnalysisResult invalidateByJobId(String jobId);

    java.util.List<AnalysisResult> invalidateByDocumentId(String documentId);

    AnalysisResult updateAnalysis(String analysisId, UpdateAnalysisCommand command);

    AnalysisResult restartAnalysis(String analysisId);

    AnalysisResult updateStatus(String analysisId, String status);

    void deleteAnalysis(String analysisId);
}
