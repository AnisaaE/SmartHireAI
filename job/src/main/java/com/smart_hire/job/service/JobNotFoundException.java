package com.smart_hire.job.service;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(Long jobId) {
        super("Job not found: " + jobId);
    }
}
