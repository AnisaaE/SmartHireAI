package com.smart_hire.ai_analysis.service;

public interface DocumentTextClient {

    String getDocumentText(String documentId);

    default void reprocessDocument(String documentId) {
        // No-op by default for clients that cannot trigger document reprocessing.
    }
}
