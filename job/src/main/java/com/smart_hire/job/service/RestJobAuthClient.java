package com.smart_hire.job.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestJobAuthClient implements JobAuthClient {

    private final RestClient restClient;

    public RestJobAuthClient(
            RestClient.Builder restClientBuilder,
            @Value("${job.integrations.auth-service.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public RecruiterSnapshot getRecruiter(Long recruiterId) {
        try {
            RecruiterPayload payload = restClient.get()
                    .uri("/api/auth/users/{id}", recruiterId)
                    .retrieve()
                    .body(RecruiterPayload.class);
            if (payload == null) {
                throw new RecruiterNotFoundException(recruiterId);
            }
            return new RecruiterSnapshot(
                    payload.id(),
                    payload.username(),
                    payload.email(),
                    payload.role(),
                    payload.active()
            );
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RecruiterNotFoundException(recruiterId);
            }
            throw exception;
        }
    }

    private record RecruiterPayload(
            Long id,
            String username,
            String email,
            String role,
            boolean active
    ) {
    }
}
