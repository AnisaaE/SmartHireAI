package com.smart_hire.ai_analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LlmAnalysisResponseParser {

    private final ObjectMapper objectMapper;

    public LlmAnalysisResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LlmCandidateAssessment parse(String rawResponse) {
        try {
            AssessmentPayload payload = objectMapper.readValue(stripCodeFences(rawResponse), AssessmentPayload.class);
            double normalizedScore = Math.max(0.0, Math.min(100.0, payload.score()));
            String reasoning = payload.reasoning() == null || payload.reasoning().isBlank()
                    ? "No reasoning provided by model"
                    : payload.reasoning().trim();

            return new LlmCandidateAssessment(normalizedScore, reasoning);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse Ollama analysis response as JSON", exception);
        }
    }

    private String stripCodeFences(String rawResponse) {
        if (rawResponse == null) {
            return "{}";
        }

        String trimmed = rawResponse.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        String withoutOpeningFence = trimmed.replaceFirst("^```(?:json)?\\s*", "");
        return withoutOpeningFence.replaceFirst("\\s*```$", "").trim();
    }

    private record AssessmentPayload(double score, String reasoning) {
    }
}
