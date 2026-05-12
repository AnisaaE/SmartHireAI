package com.smart_hire.application.service;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(Long jobId, Long candidateId) {
        super("Candidate " + candidateId + " has already applied to job " + jobId);
    }
}
