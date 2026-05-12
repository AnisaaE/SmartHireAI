package com.smart_hire.application.service;

public class CandidateNotFoundException extends RuntimeException {

    public CandidateNotFoundException(Long candidateId) {
        super("Candidate not found: " + candidateId);
    }
}
