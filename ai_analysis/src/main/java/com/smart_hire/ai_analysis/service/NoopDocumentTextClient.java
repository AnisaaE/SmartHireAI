package com.smart_hire.ai_analysis.service;

public class NoopDocumentTextClient implements DocumentTextClient {

    @Override
    public String getDocumentText(String documentId) {
        return "";
    }
}
