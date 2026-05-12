package com.smart_hire.application.service;

public interface ApplicationJobClient {

    JobSnapshot getJob(Long jobId);

    record JobSnapshot(
            Long id,
            Long recruiterId,
            String title,
            String description,
            String location,
            String employmentType,
            String status
    ) {
    }
}
