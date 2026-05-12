package com.smart_hire.job.service;

public interface JobAuthClient {

    RecruiterSnapshot getRecruiter(Long recruiterId);

    record RecruiterSnapshot(
            Long id,
            String username,
            String email,
            String role,
            boolean active
    ) {
    }
}
