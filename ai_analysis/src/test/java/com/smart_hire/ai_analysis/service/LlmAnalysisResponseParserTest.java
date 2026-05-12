package com.smart_hire.ai_analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LlmAnalysisResponseParserTest {

    private final LlmAnalysisResponseParser parser = new LlmAnalysisResponseParser(new ObjectMapper());

    @Test
    void shouldParseRawJsonResponse() {
        LlmCandidateAssessment assessment = parser.parse("""
                {"score":87.5,"reasoning":"Strong backend match"}
                """);

        assertEquals(87.5, assessment.score());
        assertEquals("Strong backend match", assessment.reasoning());
    }

    @Test
    void shouldParseMarkdownWrappedJsonResponse() {
        LlmCandidateAssessment assessment = parser.parse("""
                ```json
                {"score":105,"reasoning":"Excellent fit"}
                ```
                """);

        assertEquals(100.0, assessment.score());
        assertEquals("Excellent fit", assessment.reasoning());
    }
}
