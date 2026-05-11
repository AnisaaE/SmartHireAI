package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.CandidateAnalysis;

public record CandidateBreakdownResponse(
        Long applicationId,
        String cvDocumentId,
        double score,
        String reasoning
) {

    public static CandidateBreakdownResponse from(CandidateAnalysis candidateAnalysis) {
        return new CandidateBreakdownResponse(
                candidateAnalysis.applicationId(),
                candidateAnalysis.cvDocumentId(),
                candidateAnalysis.score(),
                candidateAnalysis.reasoning()
        );
    }
}
