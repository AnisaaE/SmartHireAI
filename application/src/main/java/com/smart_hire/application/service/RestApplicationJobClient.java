package com.smart_hire.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestApplicationJobClient implements ApplicationJobClient {

    private final RestClient restClient;

    public RestApplicationJobClient(
            RestClient.Builder restClientBuilder,
            @Value("${application.integrations.job-service.base-url:http://localhost:8084}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public JobSnapshot getJob(Long jobId) {
        try {
            JobPayload payload = restClient.get()
                    .uri("/api/jobs/{id}", jobId)
                    .retrieve()
                    .body(JobPayload.class);
            if (payload == null) {
                throw new JobUnavailableException(jobId);
            }
            return new JobSnapshot(
                    payload.id(),
                    payload.recruiterId(),
                    payload.title(),
                    payload.description(),
                    payload.location(),
                    payload.employmentType(),
                    payload.status()
            );
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new JobUnavailableException(jobId);
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
