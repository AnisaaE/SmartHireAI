package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisConfiguration;
import com.smart_hire.ai_analysis.service.StartAnalysisCommand;

import java.util.List;

public record StartAnalysisRequest(
        String jobId,
        String jobTitle,
        String jobDescription,
        List<ApplicationSnapshotRequest> applications,
        AnalysisConfigurationRequest configuration
) {

    public StartAnalysisCommand toCommand() {
        return new StartAnalysisCommand(
                jobId,
                jobTitle,
                applications.stream().map(ApplicationSnapshotRequest::toSnapshot).toList(),
                configuration.toConfiguration(),
                jobDescription
        );
    }
}
