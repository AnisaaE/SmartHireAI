package com.smart_hire.ai_analysis.service;

import java.util.List;

public record StartAnalysisCommand(
        String jobId,
        String jobTitle,
        List<ApplicationSnapshot> applications,
        AnalysisConfiguration configuration,
        String jobDescription
) {
}
