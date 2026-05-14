package com.smart_hire.ai_analysis.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnalysisServiceStartTest {

    @Test
    void shouldStartAnalysisAndRankApplicationsByScore() {
        InMemoryAnalysisRepository repository = new InMemoryAnalysisRepository();
        AnalysisScoringEngine scoringEngine = request -> List.of(
                new CandidateAnalysis(
                        101L,
                        "cv-101",
                        91.5,
                        "Strong Java and Spring background"
                ),
                new CandidateAnalysis(
                        102L,
                        "cv-102",
                        74.0,
                        "Good backend experience but limited cloud exposure"
                )
        );
        AnalysisJobClient jobClient = jobId -> new AnalysisJobClient.JobSnapshot(
                jobId,
                "Senior Java Developer",
                "Need strong backend engineers with cloud experience",
                "OPEN"
        );
        AnalysisApplicationClient applicationClient = applicationId -> switch (applicationId.intValue()) {
            case 101 -> new AnalysisApplicationClient.ApplicationDetail(101L, 1L, 501L, "cv-101", "APPLIED");
            case 102 -> new AnalysisApplicationClient.ApplicationDetail(102L, 1L, 502L, "cv-102", "APPLIED");
            default -> throw new AnalysisReferenceNotFoundException("Application not found: " + applicationId);
        };
        DocumentTextClient documentTextClient = documentId -> "Sample CV text";

        AnalysisService service = new AnalysisServiceImpl(
                repository,
                scoringEngine,
                jobClient,
                applicationClient,
                documentTextClient,
                Instant::now
        );

        StartAnalysisCommand command = new StartAnalysisCommand(
                "1",
                "Senior Java Developer",
                List.of(
                        new ApplicationSnapshot(101L, 501L, "cv-101", "Senior Java CV"),
                        new ApplicationSnapshot(102L, 502L, "cv-102", "Backend Engineer CV")
                ),
                new AnalysisConfiguration(Map.of("java", 0.7, "cloud", 0.3), List.of("java", "cloud")),
                "Need strong backend engineers with cloud experience"
        );

        AnalysisResult result = service.startAnalysis(command);

        assertNotNull(result.analysisId());
        assertEquals("1", result.jobId());
        assertEquals(List.of(101L, 102L), result.applicationIds());
        assertEquals("COMPLETED", result.status());
        assertEquals(91.5, result.applicationScores().get(101L));
        assertEquals("Strong Java and Spring background", result.applicationReasoning().get(101L));
        assertEquals("Top candidate: application 101 with score 91.50", result.summary());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }

    @Test
    void shouldReprocessDocumentWhenCvTextIsMissingBeforeStartingAnalysis() {
        InMemoryAnalysisRepository repository = new InMemoryAnalysisRepository();
        AnalysisScoringEngine scoringEngine = request -> List.of(
                new CandidateAnalysis(101L, "cv-101", 88.0, "Reprocessed CV text was available")
        );
        AnalysisJobClient jobClient = jobId -> new AnalysisJobClient.JobSnapshot(
                jobId,
                "Senior Java Developer",
                "Need strong backend engineers with cloud experience",
                "OPEN"
        );
        AnalysisApplicationClient applicationClient = applicationId -> new AnalysisApplicationClient.ApplicationDetail(
                101L,
                1L,
                501L,
                "cv-101",
                "APPLIED"
        );
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger reprocesses = new AtomicInteger();
        DocumentTextClient documentTextClient = new DocumentTextClient() {
            @Override
            public String getDocumentText(String documentId) {
                return reads.getAndIncrement() == 0 ? "" : "Recovered CV text";
            }

            @Override
            public void reprocessDocument(String documentId) {
                reprocesses.incrementAndGet();
            }
        };

        AnalysisService service = new AnalysisServiceImpl(
                repository,
                scoringEngine,
                jobClient,
                applicationClient,
                documentTextClient,
                Instant::now
        );

        StartAnalysisCommand command = new StartAnalysisCommand(
                "1",
                "Senior Java Developer",
                List.of(new ApplicationSnapshot(101L, 501L, "cv-101", "Senior Java CV")),
                new AnalysisConfiguration(Map.of("java", 1.0), List.of("java")),
                "Need strong backend engineers with cloud experience"
        );

        AnalysisResult result = service.startAnalysis(command);

        assertEquals("COMPLETED", result.status());
        assertEquals(2, reads.get());
        assertEquals(1, reprocesses.get());
    }

    @Test
    void shouldStartAnalysisWhenCvTextRemainsMissingAfterReprocess() {
        InMemoryAnalysisRepository repository = new InMemoryAnalysisRepository();
        AnalysisScoringEngine scoringEngine = request -> List.of(
                new CandidateAnalysis(101L, "cv-101", 55.0, "Analysis proceeded without extracted CV text")
        );
        AnalysisJobClient jobClient = jobId -> new AnalysisJobClient.JobSnapshot(
                jobId,
                "Senior Java Developer",
                "Need strong backend engineers with cloud experience",
                "OPEN"
        );
        AnalysisApplicationClient applicationClient = applicationId -> new AnalysisApplicationClient.ApplicationDetail(
                101L,
                1L,
                501L,
                "cv-101",
                "APPLIED"
        );
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger reprocesses = new AtomicInteger();
        DocumentTextClient documentTextClient = new DocumentTextClient() {
            @Override
            public String getDocumentText(String documentId) {
                reads.incrementAndGet();
                return "";
            }

            @Override
            public void reprocessDocument(String documentId) {
                reprocesses.incrementAndGet();
            }
        };

        AnalysisService service = new AnalysisServiceImpl(
                repository,
                scoringEngine,
                jobClient,
                applicationClient,
                documentTextClient,
                Instant::now
        );

        StartAnalysisCommand command = new StartAnalysisCommand(
                "1",
                "Senior Java Developer",
                List.of(new ApplicationSnapshot(101L, 501L, "cv-101", "Senior Java CV")),
                new AnalysisConfiguration(Map.of("java", 1.0), List.of("java")),
                "Need strong backend engineers with cloud experience"
        );

        AnalysisResult result = service.startAnalysis(command);

        assertEquals("COMPLETED", result.status());
        assertEquals(2, reads.get());
        assertEquals(1, reprocesses.get());
    }
}
