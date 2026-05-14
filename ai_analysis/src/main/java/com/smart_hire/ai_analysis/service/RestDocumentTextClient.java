package com.smart_hire.ai_analysis.service;

import com.smart_hire.ai_analysis.config.AnalysisRuntimeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class RestDocumentTextClient implements DocumentTextClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestDocumentTextClient.class);

    private final RestClient restClient;

    public RestDocumentTextClient(AnalysisRuntimeProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.documentServiceBaseUrl())
                .build();
    }

    @Override
    public String getDocumentText(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return "";
        }

        try {
            DocumentContentPayload payload = restClient.get()
                    .uri("/api/documents/content/{id}", documentId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(DocumentContentPayload.class);

            return payload == null || payload.rawTextContent() == null ? "" : payload.rawTextContent();
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AnalysisReferenceNotFoundException("Document not found: " + documentId);
            }
            LOGGER.warn("Falling back to empty document text for {} because document-service returned status {}: {}",
                    documentId,
                    exception.getStatusCode(),
                    exception.getMessage());
            return "";
        }
        catch (RestClientException exception) {
            LOGGER.warn("Falling back to empty document text for {} because document-service call failed: {}",
                    documentId,
                    exception.getMessage());
            return "";
        }
    }

    @Override
    public void reprocessDocument(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return;
        }

        try {
            restClient.put()
                    .uri("/api/documents/{id}/reprocess", documentId)
                    .retrieve()
                    .toBodilessEntity();
        }
        catch (RestClientException exception) {
            LOGGER.warn("Failed to reprocess document {} before analysis: {}", documentId, exception.getMessage());
        }
    }

    private record DocumentContentPayload(String documentId, String rawTextContent) {
    }
}
