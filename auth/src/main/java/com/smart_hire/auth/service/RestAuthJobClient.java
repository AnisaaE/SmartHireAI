package com.smart_hire.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class RestAuthJobClient implements AuthJobClient {

    private final RestClient restClient;

    public RestAuthJobClient(
            RestClient.Builder restClientBuilder,
            @Value("${auth.integrations.job-service.base-url:http://localhost:8084}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<JobSummary> getJobsByRecruiterId(Long recruiterId) {
        JobSummaryPayload[] payload = restClient.get()
                .uri("/api/jobs/recruiter/{recruiterId}", recruiterId)
                .retrieve()
                .body(JobSummaryPayload[].class);
        if (payload == null) {
            return List.of();
        }
        return Arrays.stream(payload)
                .map(job -> new JobSummary(job.id(), job.recruiterId(), job.title(), job.status()))
                .toList();
    }

    @Override
    public void archiveJob(Long jobId) {
        restClient.put()
                .uri("/api/jobs/{jobId}/status", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateJobStatusPayload("ARCHIVED"))
                .retrieve()
                .toBodilessEntity();
    }

    private record JobSummaryPayload(
            Long id,
            Long recruiterId,
            String title,
            String status
    ) {
    }

    private record UpdateJobStatusPayload(String status) {
    }
}
