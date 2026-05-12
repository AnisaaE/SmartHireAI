package com.smart_hire.application.service;

public class JobUnavailableException extends RuntimeException {

    public JobUnavailableException(Long jobId) {
        super("Job is not open for applications: " + jobId);
    }
}
