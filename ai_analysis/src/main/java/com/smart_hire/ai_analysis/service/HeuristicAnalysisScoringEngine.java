package com.smart_hire.ai_analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeuristicAnalysisScoringEngine implements AnalysisScoringEngine {

    @Override
    public List<CandidateAnalysis> analyze(StartAnalysisCommand request) {
        List<CandidateAnalysis> analyses = new ArrayList<>();
        List<String> criteria = request.configuration() == null ? List.of() : request.configuration().evaluationCriteria();

        for (ApplicationSnapshot application : request.applications()) {
            double score = scoreCandidate(application, request, criteria);
            analyses.add(new CandidateAnalysis(
                    application.applicationId(),
                    application.cvDocumentId(),
                    score,
                    buildReasoning(application, criteria, score)
            ));
        }

        return analyses;
    }

    private double scoreCandidate(
            ApplicationSnapshot application,
            StartAnalysisCommand request,
            List<String> criteria
    ) {
        String searchableText = (
                safe(request.jobTitle()) + " " +
                safe(request.jobDescription()) + " " +
                safe(application.candidateLabel()) + " " +
                safe(application.cvDocumentId())
        ).toLowerCase(Locale.ROOT);

        double matchedWeight = 0.0;
        double totalWeight = 0.0;
        for (String criterion : criteria) {
            double weight = request.configuration().scoringWeights().getOrDefault(criterion, 1.0);
            totalWeight += weight;
            if (searchableText.contains(criterion.toLowerCase(Locale.ROOT))) {
                matchedWeight += weight;
            }
        }

        if (totalWeight == 0.0) {
            return 50.0;
        }

        return Math.round((matchedWeight / totalWeight) * 10000.0) / 100.0;
    }

    private String buildReasoning(ApplicationSnapshot application, List<String> criteria, double score) {
        if (criteria.isEmpty()) {
            return "Scored %.2f based on default heuristic evaluation for %s"
                    .formatted(score, application.candidateLabel());
        }

        return "Scored %.2f for %s against criteria: %s"
                .formatted(score, application.candidateLabel(), String.join(", ", criteria));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
