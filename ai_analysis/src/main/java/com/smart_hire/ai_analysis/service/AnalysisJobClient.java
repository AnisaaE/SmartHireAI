package com.smart_hire.ai_analysis.service;

public interface AnalysisJobClient {

    JobSnapshot getJob(String jobId);

    record JobSnapshot(
            String id,
            String title,
            String description,
            String status
    ) {
    }
}
