package com.smart_hire.auth.service;

import java.util.List;

public interface AuthJobClient {

    List<JobSummary> getJobsByRecruiterId(Long recruiterId);

    void archiveJob(Long jobId);

    record JobSummary(
            Long id,
            Long recruiterId,
            String title,
            String status
    ) {
    }
}
