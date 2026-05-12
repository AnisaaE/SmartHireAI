package com.smart_hire.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestDocumentApplicationClient implements DocumentApplicationClient {

    private final RestClient restClient;

    public RestDocumentApplicationClient(
            RestClient.Builder restClientBuilder,
            @Value("${document.integrations.application-service.base-url:http://localhost:8085}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean hasActiveApplications(String documentId) {
        ActiveDocumentDependencyPayload payload = restClient.get()
                .uri("/api/applications/document/{documentId}/active-exists", documentId)
                .retrieve()
                .body(ActiveDocumentDependencyPayload.class);
        return payload != null && payload.exists();
    }

    private record ActiveDocumentDependencyPayload(boolean exists) {
    }
}
