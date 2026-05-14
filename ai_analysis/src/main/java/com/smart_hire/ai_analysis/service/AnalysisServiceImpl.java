package com.smart_hire.ai_analysis.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.UUID;

public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisScoringEngine scoringEngine;
    private final AnalysisJobClient jobClient;
    private final AnalysisApplicationClient applicationClient;
    private final DocumentTextClient documentTextClient;
    private final Supplier<Instant> nowSupplier;

    public AnalysisServiceImpl(
            AnalysisRepository analysisRepository,
            AnalysisScoringEngine scoringEngine,
            AnalysisJobClient jobClient,
            AnalysisApplicationClient applicationClient,
            DocumentTextClient documentTextClient,
            Supplier<Instant> nowSupplier
    ) {
        this.analysisRepository = analysisRepository;
        this.scoringEngine = scoringEngine;
        this.jobClient = jobClient;
        this.applicationClient = applicationClient;
        this.documentTextClient = documentTextClient;
        this.nowSupplier = nowSupplier;
    }

    @Override
    public AnalysisResult startAnalysis(StartAnalysisCommand command) {
        StartAnalysisCommand validatedCommand = validateAndHydrate(command);
        AnalysisResult result = createResult(UUID.randomUUID().toString(), validatedCommand, nowSupplier.get());
        analysisRepository.saveCommand(result.analysisId(), validatedCommand);
        return analysisRepository.save(result);
    }

    @Override
    public AnalysisResult getAnalysis(String analysisId) {
        AnalysisResult result = analysisRepository.findById(analysisId);
        if (result == null) {
            throw new AnalysisNotFoundException(analysisId);
        }
        return refreshInvalidationState(result);
    }

    @Override
    public AnalysisResult getReport(String jobId) {
        AnalysisResult result = analysisRepository.findByJobId(jobId);
        if (result == null) {
            throw new AnalysisNotFoundException(jobId);
        }
        return refreshInvalidationState(result);
    }

    @Override
    public List<CandidateAnalysis> getCandidates(String analysisId) {
        return getAnalysis(analysisId).toCandidateAnalyses();
    }

    @Override
    public AnalysisResult invalidateByJobId(String jobId) {
        AnalysisResult result = analysisRepository.findByJobId(jobId);
        if (result == null) {
            throw new AnalysisNotFoundException(jobId);
        }
        return invalidate(result, "Job content changed for " + jobId);
    }

    @Override
    public List<AnalysisResult> invalidateByDocumentId(String documentId) {
        List<AnalysisResult> matches = analysisRepository.findAll().stream()
                .filter(result -> {
                    StartAnalysisCommand command = analysisRepository.findCommandById(result.analysisId());
                    return command != null && command.applications().stream()
                            .anyMatch(application -> documentId.equals(application.cvDocumentId()));
                })
                .toList();
        return matches.stream()
                .map(result -> invalidate(result, "Document content changed for " + documentId))
                .toList();
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
        StartAnalysisCommand validatedCommand = validateAndHydrate(updatedCommand);
        analysisRepository.saveCommand(analysisId, validatedCommand);
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
        StartAnalysisCommand command = validateAndHydrate(getStoredCommand(analysisId));
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

    private StartAnalysisCommand validateAndHydrate(StartAnalysisCommand command) {
        AnalysisJobClient.JobSnapshot job = jobClient.getJob(command.jobId());
        List<ApplicationSnapshot> validatedApplications = command.applications().stream()
                .map(application -> validateApplication(job.id(), application))
                .toList();
        return new StartAnalysisCommand(
                job.id(),
                job.title(),
                validatedApplications,
                command.configuration(),
                job.description()
        );
    }

    private ApplicationSnapshot validateApplication(String jobId, ApplicationSnapshot requestedApplication) {
        AnalysisApplicationClient.ApplicationDetail application = applicationClient.getApplication(requestedApplication.applicationId());
        if (!Objects.equals(String.valueOf(application.jobId()), jobId)) {
            throw new AnalysisReferenceValidationException(
                    "Application %d is not linked to job %s".formatted(requestedApplication.applicationId(), jobId)
            );
        }
        if (!Objects.equals(application.candidateId(), requestedApplication.candidateId())) {
            throw new AnalysisReferenceValidationException(
                    "Application %d is not linked to candidate %d".formatted(
                            requestedApplication.applicationId(),
                            requestedApplication.candidateId()
                    )
            );
        }
        if (!Objects.equals(application.cvDocumentId(), requestedApplication.cvDocumentId())) {
            throw new AnalysisReferenceValidationException(
                    "Application %d CV reference does not match document %s".formatted(
                            requestedApplication.applicationId(),
                            requestedApplication.cvDocumentId()
                    )
            );
        }
        requireDocumentText(application.cvDocumentId());
        return requestedApplication;
    }

    private String requireDocumentText(String documentId) {
        String documentText = documentTextClient.getDocumentText(documentId);
        if (documentText != null && !documentText.isBlank()) {
            return documentText;
        }

        documentTextClient.reprocessDocument(documentId);
        return documentTextClient.getDocumentText(documentId);
    }

    private AnalysisResult refreshInvalidationState(AnalysisResult result) {
        if (AnalysisStatus.INVALIDATED.name().equals(result.status())) {
            return result;
        }

        try {
            StartAnalysisCommand storedCommand = getStoredCommand(result.analysisId());
            StartAnalysisCommand validatedCommand = validateAndHydrate(storedCommand);
            if (!Objects.equals(storedCommand.jobTitle(), validatedCommand.jobTitle())
                    || !Objects.equals(storedCommand.jobDescription(), validatedCommand.jobDescription())) {
                return invalidate(result, "Job content changed for " + result.jobId());
            }
            return result;
        }
        catch (AnalysisReferenceNotFoundException | AnalysisReferenceValidationException exception) {
            return invalidate(result, exception.getMessage());
        }
    }

    private AnalysisResult invalidate(AnalysisResult existing, String reason) {
        AnalysisResult invalidated = new AnalysisResult(
                existing.analysisId(),
                existing.jobId(),
                existing.applicationIds(),
                existing.applicationScores(),
                existing.applicationReasoning(),
                AnalysisStatus.INVALIDATED.name(),
                "Analysis invalidated: " + reason,
                existing.createdAt(),
                nowSupplier.get()
        );
        return analysisRepository.save(invalidated);
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
        return String.format(
                Locale.ROOT,
                "Top candidate: application %d with score %.2f",
                topCandidate.applicationId(),
                topCandidate.score()
        );
    }
}
