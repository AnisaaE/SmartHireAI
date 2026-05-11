package com.smart_hire.ai_analysis.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.UUID;

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
        AnalysisResult result = createResult(UUID.randomUUID().toString(), command, nowSupplier.get());
        analysisRepository.saveCommand(result.analysisId(), command);
        return analysisRepository.save(result);
    }

    @Override
    public AnalysisResult getAnalysis(String analysisId) {
        AnalysisResult result = analysisRepository.findById(analysisId);
        if (result == null) {
            throw new AnalysisNotFoundException(analysisId);
        }
        return result;
    }

    @Override
    public AnalysisResult getReport(String jobId) {
        AnalysisResult result = analysisRepository.findByJobId(jobId);
        if (result == null) {
            throw new AnalysisNotFoundException(jobId);
        }
        return result;
    }

    @Override
    public List<CandidateAnalysis> getCandidates(String analysisId) {
        return getAnalysis(analysisId).toCandidateAnalyses();
    }

    @Override
    public AnalysisResult updateAnalysis(String analysisId, UpdateAnalysisCommand command) {
        AnalysisResult existing = getAnalysis(analysisId);
        StartAnalysisCommand existingCommand = getStoredCommand(analysisId);
        StartAnalysisCommand updatedCommand = new StartAnalysisCommand(
                existingCommand.jobId(),
                existingCommand.jobTitle(),
                existingCommand.applications(),
                command.configuration(),
                existingCommand.jobDescription()
        );
        analysisRepository.saveCommand(analysisId, updatedCommand);
        AnalysisResult updated = copyResult(
                existing,
                existing.analysisId(),
                existing.status(),
                existing.createdAt(),
                nowSupplier.get()
        );
        return analysisRepository.save(updated);
    }

    @Override
    public AnalysisResult restartAnalysis(String analysisId) {
        getAnalysis(analysisId);
        StartAnalysisCommand command = getStoredCommand(analysisId);
        Instant now = nowSupplier.get();
        String restartedAnalysisId = analysisId + "-restarted";
        AnalysisResult restarted = createResult(restartedAnalysisId, command, now);
        analysisRepository.saveCommand(restartedAnalysisId, command);
        return analysisRepository.save(restarted);
    }

    @Override
    public AnalysisResult updateStatus(String analysisId, String status) {
        AnalysisResult existing = getAnalysis(analysisId);
        AnalysisResult updated = copyResult(
                existing,
                existing.analysisId(),
                AnalysisStatus.valueOf(status).name(),
                existing.createdAt(),
                nowSupplier.get()
        );
        return analysisRepository.save(updated);
    }

    @Override
    public void deleteAnalysis(String analysisId) {
        getAnalysis(analysisId);
        analysisRepository.deleteById(analysisId);
    }

    private AnalysisResult createResult(String analysisId, StartAnalysisCommand command, Instant now) {
        List<CandidateAnalysis> rankedCandidates = scoringEngine.analyze(command).stream()
                .sorted(Comparator.comparingDouble(CandidateAnalysis::score).reversed())
                .toList();

        return new AnalysisResult(
                analysisId,
                command.jobId(),
                rankedCandidates.stream().map(CandidateAnalysis::applicationId).toList(),
                buildScoreMap(rankedCandidates),
                buildReasoningMap(rankedCandidates),
                AnalysisStatus.COMPLETED.name(),
                buildSummary(rankedCandidates),
                now,
                now
        );
    }

    private AnalysisResult copyResult(
            AnalysisResult source,
            String analysisId,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new AnalysisResult(
                analysisId,
                source.jobId(),
                source.applicationIds(),
                source.applicationScores(),
                source.applicationReasoning(),
                status,
                source.summary(),
                createdAt,
                updatedAt
        );
    }

    private StartAnalysisCommand getStoredCommand(String analysisId) {
        StartAnalysisCommand command = analysisRepository.findCommandById(analysisId);
        if (command == null) {
            throw new AnalysisNotFoundException(analysisId);
        }
        return command;
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
