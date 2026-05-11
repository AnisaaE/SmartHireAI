package com.smart_hire.ai_analysis.service;

public class AnalysisNotFoundException extends RuntimeException {

    public AnalysisNotFoundException(String identifier) {
        super("Analysis not found: " + identifier);
    }
}
