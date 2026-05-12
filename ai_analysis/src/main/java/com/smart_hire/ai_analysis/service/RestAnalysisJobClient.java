package com.smart_hire.ai_analysis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestAnalysisJobClient implements AnalysisJobClient {

    private final RestClient restClient;

    public RestAnalysisJobClient(
            RestClient.Builder restClientBuilder,
            @Value("${analysis.job-service-base-url:http://localhost:8084}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public JobSnapshot getJob(String jobId) {
        try {
            JobPayload payload = restClient.get()
                    .uri("/api/jobs/{jobId}", Long.parseLong(jobId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JobPayload.class);
            if (payload == null) {
                throw new AnalysisReferenceNotFoundException("Job not found: " + jobId);
            }
            return new JobSnapshot(String.valueOf(payload.id()), payload.title(), payload.description(), payload.status());
        }
        catch (NumberFormatException exception) {
            throw new AnalysisReferenceValidationException("Job id must be numeric: " + jobId);
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AnalysisReferenceNotFoundException("Job not found: " + jobId);
            }
            throw exception;
        }
    }

    private record JobPayload(
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
