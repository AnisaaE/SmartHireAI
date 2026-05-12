package com.smart_hire.ai_analysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LlmAnalysisScoringEngine implements AnalysisScoringEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmAnalysisScoringEngine.class);

    private final ChatClient chatClient;
    private final LlmAnalysisResponseParser responseParser;
    private final DocumentTextClient documentTextClient;
    private final AnalysisScoringEngine fallbackScoringEngine;
    private final String model;
    private final double temperature;

    public LlmAnalysisScoringEngine(
            ChatClient chatClient,
            LlmAnalysisResponseParser responseParser,
            DocumentTextClient documentTextClient,
            AnalysisScoringEngine fallbackScoringEngine,
            String model,
            double temperature
    ) {
        this.chatClient = chatClient;
        this.responseParser = responseParser;
        this.documentTextClient = documentTextClient;
        this.fallbackScoringEngine = fallbackScoringEngine;
        this.model = model;
        this.temperature = temperature;
    }

    @Override
    public List<CandidateAnalysis> analyze(StartAnalysisCommand request) {
        try {
            return analyzeWithLlm(request);
        }
        catch (RuntimeException exception) {
            LOGGER.warn("Falling back to heuristic analysis because LLM scoring failed: {}", exception.getMessage());
            return fallbackScoringEngine.analyze(request);
        }
    }

    private List<CandidateAnalysis> analyzeWithLlm(StartAnalysisCommand request) {
        List<CandidateAnalysis> analyses = new ArrayList<>();
        for (ApplicationSnapshot application : request.applications()) {
            String cvText = documentTextClient.getDocumentText(application.cvDocumentId());
            String response = chatClient.prompt()
                    .system(systemPrompt())
                    .user(userPrompt(request, application, cvText))
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .temperature(temperature))
                    .call()
                    .content();

            LlmCandidateAssessment assessment = responseParser.parse(response);
            analyses.add(new CandidateAnalysis(
                    application.applicationId(),
                    application.cvDocumentId(),
                    assessment.score(),
                    assessment.reasoning()
            ));
        }

        return analyses;
    }

    private String systemPrompt() {
        return """
                You are an expert technical recruiter.
                Evaluate one candidate against one job.
                Return only valid JSON with this exact schema:
                {
                  "score": 0-100,
                  "reasoning": "short explanation"
                }
                Do not include markdown, prose, or extra keys.
                """;
    }

    private String userPrompt(StartAnalysisCommand request, ApplicationSnapshot application, String cvText) {
        return """
                Job title:
                %s

                Job description:
                %s

                Evaluation criteria:
                %s

                Scoring weights:
                %s

                Candidate label:
                %s

                Candidate CV text:
                %s

                Score the candidate from 0 to 100 based on fit for this job.
                """.formatted(
                request.jobTitle(),
                request.jobDescription(),
                request.configuration().evaluationCriteria(),
                stringifyWeights(request.configuration().scoringWeights()),
                application.candidateLabel(),
                cvText == null || cvText.isBlank() ? "No CV text available" : cvText
        );
    }

    private String stringifyWeights(Map<String, Double> scoringWeights) {
        if (scoringWeights == null || scoringWeights.isEmpty()) {
            return "{}";
        }
        return scoringWeights.toString();
    }
}
