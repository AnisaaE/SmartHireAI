package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisResult;

public final class AnalysisResponseMapper {

    private AnalysisResponseMapper() {
    }

    public static AnalysisResponse toResponse(AnalysisResult result) {
        return new AnalysisResponse(
                result.analysisId(),
                result.jobId(),
                result.applicationIds(),
                result.applicationScores(),
                result.applicationReasoning(),
                result.status(),
                result.summary(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
