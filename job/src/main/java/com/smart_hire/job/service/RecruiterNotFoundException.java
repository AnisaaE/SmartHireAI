package com.smart_hire.job.service;

public class RecruiterNotFoundException extends RuntimeException {

    public RecruiterNotFoundException(Long recruiterId) {
        super("Recruiter not found: " + recruiterId);
    }
}
