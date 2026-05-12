package com.smart_hire.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestApplicationAuthClient implements ApplicationAuthClient {

    private final RestClient restClient;

    public RestApplicationAuthClient(
            RestClient.Builder restClientBuilder,
            @Value("${application.integrations.auth-service.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public CandidateSnapshot getCandidate(Long candidateId) {
        try {
            CandidatePayload payload = restClient.get()
                    .uri("/api/auth/users/{id}", candidateId)
                    .retrieve()
                    .body(CandidatePayload.class);
            if (payload == null) {
                throw new CandidateNotFoundException(candidateId);
            }
            return new CandidateSnapshot(
                    payload.id(),
                    payload.username(),
                    payload.email(),
                    payload.role(),
                    payload.active()
            );
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CandidateNotFoundException(candidateId);
            }
            throw exception;
        }
    }

    private record CandidatePayload(
            Long id,
            String username,
            String email,
            String role,
            boolean active
    ) {
    }
}
