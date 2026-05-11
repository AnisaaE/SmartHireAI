package com.smart_hire.ai_analysis.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisScoringEngine scoringEngine;
    private final Supplier<Instant> nowSupplier;

    public AnalysisServiceImpl(
            AnalysisRepository analysisRepository,
            AnalysisScoringEngine scoringEngine,
            Supplier<Instant> nowSupplier
    ) {
        this.analysisRepository = analysisRepository;
        this.scoringEngine = scoringEngine;
        this.nowSupplier = nowSupplier;
    }

    @Override
    public AnalysisResult startAnalysis(StartAnalysisCommand command) {
        List<CandidateAnalysis> rankedCandidates = scoringEngine.analyze(command).stream()
                .sorted(Comparator.comparingDouble(CandidateAnalysis::score).reversed())
                .toList();
        Instant now = nowSupplier.get();

        AnalysisResult result = new AnalysisResult(
                UUID.randomUUID().toString(),
                command.jobId(),
                rankedCandidates.stream().map(CandidateAnalysis::applicationId).toList(),
                buildScoreMap(rankedCandidates),
                buildReasoningMap(rankedCandidates),
                AnalysisStatus.COMPLETED.name(),
                buildSummary(rankedCandidates),
                now,
                now
        );

        return analysisRepository.save(result);
    }

    private Map<Long, Double> buildScoreMap(List<CandidateAnalysis> rankedCandidates) {
        Map<Long, Double> scores = new LinkedHashMap<>();
        for (CandidateAnalysis candidate : rankedCandidates) {
            scores.put(candidate.applicationId(), candidate.score());
        }
        return scores;
    }

    private Map<Long, String> buildReasoningMap(List<CandidateAnalysis> rankedCandidates) {
        Map<Long, String> reasoning = new LinkedHashMap<>();
        for (CandidateAnalysis candidate : rankedCandidates) {
            reasoning.put(candidate.applicationId(), candidate.reasoning());
        }
        return reasoning;
    }

    private String buildSummary(List<CandidateAnalysis> rankedCandidates) {
        if (rankedCandidates.isEmpty()) {
            return "No candidates available for analysis";
        }

        CandidateAnalysis topCandidate = rankedCandidates.getFirst();
        return "Top candidate: application %d with score %.2f"
                .formatted(topCandidate.applicationId(), topCandidate.score());
    }
}
