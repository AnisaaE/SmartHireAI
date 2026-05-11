package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.AnalysisConfiguration;
import com.smart_hire.ai_analysis.service.AnalysisResult;
import com.smart_hire.ai_analysis.service.AnalysisService;
import com.smart_hire.ai_analysis.service.CandidateAnalysis;
import com.smart_hire.ai_analysis.service.StartAnalysisCommand;
import com.smart_hire.ai_analysis.service.UpdateAnalysisCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisControllerUpdateAndRestartApiTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisController(new StubAnalysisService())).build();
    }

    @Test
    void shouldUpdateAnalysisConfiguration() throws Exception {
        mockMvc.perform(put("/api/analysis/analysis-77")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "scoringWeights": {
                                    "backend": 0.9,
                                    "leadership": 0.1
                                  },
                                  "evaluationCriteria": ["backend", "leadership"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value("analysis-77"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldRestartAnalysis() throws Exception {
        mockMvc.perform(put("/api/analysis/analysis-77/restart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value("analysis-77-restarted"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private static final class StubAnalysisService implements AnalysisService {

        @Override
        public AnalysisResult startAnalysis(StartAnalysisCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AnalysisResult getAnalysis(String analysisId) {
            return sampleResult("analysis-77");
        }

        @Override
        public AnalysisResult getReport(String jobId) {
            return sampleResult("analysis-77");
        }

        @Override
        public List<CandidateAnalysis> getCandidates(String analysisId) {
            return List.of();
        }

        @Override
        public AnalysisResult updateAnalysis(String analysisId, UpdateAnalysisCommand command) {
            return sampleResult(analysisId);
        }

        @Override
        public AnalysisResult restartAnalysis(String analysisId) {
            return sampleResult("analysis-77-restarted");
        }

        @Override
        public AnalysisResult updateStatus(String analysisId, String status) {
            return sampleResult(analysisId);
        }

        @Override
        public void deleteAnalysis(String analysisId) {
        }

        private AnalysisResult sampleResult(String analysisId) {
            return new AnalysisResult(
                    analysisId,
                    "job-77",
                    List.of(401L),
                    Map.of(401L, 88.0),
                    Map.of(401L, "Strong backend background"),
                    "COMPLETED",
                    "Top candidate: application 401 with score 88.00",
                    Instant.parse("2026-05-11T10:15:30Z"),
                    Instant.parse("2026-05-11T10:25:30Z")
            );
        }
    }
}
