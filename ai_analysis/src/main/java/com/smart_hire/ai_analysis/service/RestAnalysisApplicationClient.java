package com.smart_hire.ai_analysis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestAnalysisApplicationClient implements AnalysisApplicationClient {

    private final RestClient restClient;

    public RestAnalysisApplicationClient(
            RestClient.Builder restClientBuilder,
            @Value("${analysis.application-service-base-url:http://localhost:8085}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public ApplicationDetail getApplication(Long applicationId) {
        try {
            ApplicationPayload payload = restClient.get()
                    .uri("/api/applications/{applicationId}", applicationId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ApplicationPayload.class);
            if (payload == null) {
                throw new AnalysisReferenceNotFoundException("Application not found: " + applicationId);
            }
            return new ApplicationDetail(
                    payload.id(),
                    payload.jobId(),
                    payload.candidateId(),
                    payload.cvDocumentId(),
                    payload.status()
            );
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AnalysisReferenceNotFoundException("Application not found: " + applicationId);
            }
            throw exception;
        }
    }

    private record ApplicationPayload(
            Long id,
            Long jobId,
            Long candidateId,
            String cvDocumentId,
            String status
    ) {
    }
}
