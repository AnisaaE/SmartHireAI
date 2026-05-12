package com.smart_hire.application.service;

public interface ApplicationAuthClient {

    CandidateSnapshot getCandidate(Long candidateId);

    record CandidateSnapshot(
            Long id,
            String username,
            String email,
            String role,
            boolean active
    ) {
    }
}
