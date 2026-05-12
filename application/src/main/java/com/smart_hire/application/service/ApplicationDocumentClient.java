package com.smart_hire.application.service;

public interface ApplicationDocumentClient {

    DocumentMetadata getDocumentMetadata(String documentId);

    String getDocumentContent(String documentId);

    record DocumentMetadata(
            String id,
            String ownerId,
            String type,
            String title
    ) {
    }
}
