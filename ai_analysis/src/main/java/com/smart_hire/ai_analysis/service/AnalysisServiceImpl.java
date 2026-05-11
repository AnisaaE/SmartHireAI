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

    @Override
    public AnalysisResult getAnalysis(String analysisId) {
        return analysisRepository.findById(analysisId);
    }

    @Override
    public AnalysisResult getReport(String jobId) {
        return analysisRepository.findByJobId(jobId);
    }

    @Override
    public List<CandidateAnalysis> getCandidates(String analysisId) {
        AnalysisResult result = getAnalysis(analysisId);

        return result.applicationIds().stream()
                .map(applicationId -> new CandidateAnalysis(
                        applicationId,
                        null,
                        result.applicationScores().getOrDefault(applicationId, 0.0),
                        result.applicationReasoning().get(applicationId)
                ))
                .toList();
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
