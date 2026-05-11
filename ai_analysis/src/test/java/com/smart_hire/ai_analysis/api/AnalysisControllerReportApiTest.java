package com.smart_hire.ai_analysis.api;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisControllerReportApiTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisController(new StubAnalysisService())).build();
    }

    @Test
    void shouldGetReportByJobId() throws Exception {
        mockMvc.perform(get("/api/analysis/report/job-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value("analysis-77"))
                .andExpect(jsonPath("$.jobId").value("job-77"));
    }

    @Test
    void shouldGetCandidateBreakdown() throws Exception {
        mockMvc.perform(get("/api/analysis/analysis-77/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(401))
                .andExpect(jsonPath("$[0].score").value(88.0))
                .andExpect(jsonPath("$[0].reasoning").value("Strong backend background"));
    }

    private static final class StubAnalysisService implements AnalysisService {

        @Override
        public AnalysisResult startAnalysis(StartAnalysisCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AnalysisResult getAnalysis(String analysisId) {
            return sampleResult();
        }

        @Override
        public AnalysisResult getReport(String jobId) {
            return sampleResult();
        }

        @Override
        public List<CandidateAnalysis> getCandidates(String analysisId) {
            return List.of(new CandidateAnalysis(401L, "cv-401", 88.0, "Strong backend background"));
        }

        @Override
        public AnalysisResult updateAnalysis(String analysisId, UpdateAnalysisCommand command) {
            return sampleResult();
        }

        @Override
        public AnalysisResult restartAnalysis(String analysisId) {
            return sampleResult();
        }

        @Override
        public AnalysisResult updateStatus(String analysisId, String status) {
            return sampleResult();
        }

        @Override
        public void deleteAnalysis(String analysisId) {
        }

        private AnalysisResult sampleResult() {
            return new AnalysisResult(
                    "analysis-77",
                    "job-77",
                    List.of(401L),
                    Map.of(401L, 88.0),
                    Map.of(401L, "Strong backend background"),
                    "COMPLETED",
                    "Top candidate: application 401 with score 88.00",
                    Instant.parse("2026-05-11T10:15:30Z"),
                    Instant.parse("2026-05-11T10:15:30Z")
            );
        }
    }
}
