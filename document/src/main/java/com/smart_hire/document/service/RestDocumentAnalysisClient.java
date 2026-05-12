package com.smart_hire.document.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestDocumentAnalysisClient implements DocumentAnalysisClient {

    private final RestClient restClient;

    public RestDocumentAnalysisClient(
            RestClient.Builder restClientBuilder,
            @Value("${document.integrations.ai-analysis-service.base-url:http://localhost:8083}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public void invalidateByDocumentId(String documentId) {
        restClient.put()
                .uri("/api/analysis/invalidate/document/{documentId}", documentId)
                .retrieve()
                .toBodilessEntity();
    }
}
