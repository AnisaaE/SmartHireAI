package com.smart_hire.ai_analysis.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

        AnalysisService service = new AnalysisServiceImpl(repository, scoringEngine, Instant::now);

        StartAnalysisCommand command = new StartAnalysisCommand(
                "job-1",
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
        assertEquals("job-1", result.jobId());
        assertEquals(List.of(101L, 102L), result.applicationIds());
        assertEquals("COMPLETED", result.status());
        assertEquals(91.5, result.applicationScores().get(101L));
        assertEquals("Strong Java and Spring background", result.applicationReasoning().get(101L));
        assertEquals("Top candidate: application 101 with score 91.50", result.summary());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }
}
