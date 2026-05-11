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

        Map<Long, Double> scores = new LinkedHashMap<>();
        Map<Long, String> reasoning = new LinkedHashMap<>();
        for (CandidateAnalysis candidate : rankedCandidates) {
            scores.put(candidate.applicationId(), candidate.score());
            reasoning.put(candidate.applicationId(), candidate.reasoning());
        }

        Long topApplicationId = rankedCandidates.isEmpty() ? null : rankedCandidates.getFirst().applicationId();
        double topScore = rankedCandidates.isEmpty() ? 0.0 : rankedCandidates.getFirst().score();

        AnalysisResult result = new AnalysisResult(
                UUID.randomUUID().toString(),
                command.jobId(),
                rankedCandidates.stream().map(CandidateAnalysis::applicationId).toList(),
                scores,
                reasoning,
                "COMPLETED",
                topApplicationId == null
                        ? "No candidates available for analysis"
                        : "Top candidate: application %d with score %.2f".formatted(topApplicationId, topScore),
                now,
                now
        );

        return analysisRepository.save(result);
    }
}
