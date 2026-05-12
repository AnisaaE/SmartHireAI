package com.smart_hire.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestApplicationDocumentClient implements ApplicationDocumentClient {

    private final RestClient restClient;

    public RestApplicationDocumentClient(
            RestClient.Builder restClientBuilder,
            @Value("${application.integrations.document-service.base-url:http://localhost:8082}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public DocumentMetadata getDocumentMetadata(String documentId) {
        try {
            DocumentMetadataPayload payload = restClient.get()
                    .uri("/api/documents/{id}", documentId)
                    .retrieve()
                    .body(DocumentMetadataPayload.class);
            if (payload == null) {
                throw new InvalidCvDocumentException("CV document metadata is missing: " + documentId);
            }
            return new DocumentMetadata(payload.id(), payload.ownerId(), payload.type(), payload.title(), payload.status());
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new InvalidCvDocumentException("CV document not found: " + documentId);
            }
            throw exception;
        }
    }

    @Override
    public String getDocumentContent(String documentId) {
        try {
            DocumentContentPayload payload = restClient.get()
                    .uri("/api/documents/content/{id}", documentId)
                    .retrieve()
                    .body(DocumentContentPayload.class);
            if (payload == null) {
                return "";
            }
            return payload.rawTextContent();
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new InvalidCvDocumentException("CV document text not found: " + documentId);
            }
            throw exception;
        }
    }

    private record DocumentMetadataPayload(
            String id,
            String ownerId,
            String type,
            String title,
            String status
    ) {
    }

    private record DocumentContentPayload(
            String documentId,
            String rawTextContent
    ) {
    }
}
