package com.smart_hire.job.service;

public class JobDeletionConflictException extends RuntimeException {

    public JobDeletionConflictException(Long jobId) {
        super("Job cannot be deleted while applications exist unless it is CLOSED or ARCHIVED: " + jobId);
    }
}
