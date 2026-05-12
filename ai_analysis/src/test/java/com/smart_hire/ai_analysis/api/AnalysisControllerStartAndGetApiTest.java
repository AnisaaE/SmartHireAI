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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisControllerStartAndGetApiTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AnalysisService analysisService = new StubAnalysisService();
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisController(analysisService)).build();
    }

    @Test
    void shouldStartAnalysis() throws Exception {
        mockMvc.perform(post("/api/analysis/start")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "jobId": "job-77",
                                  "jobTitle": "Platform Engineer",
                                  "jobDescription": "Build resilient backend services",
                                  "applications": [
                                    {
                                      "applicationId": 401,
                                      "candidateId": 9001,
                                      "cvDocumentId": "cv-401",
                                      "candidateLabel": "Alice"
                                    }
                                  ],
                                  "configuration": {
                                    "scoringWeights": {
                                      "backend": 0.8
                                    },
                                    "evaluationCriteria": ["backend"]
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analysisId").value("analysis-77"))
                .andExpect(jsonPath("$.jobId").value("job-77"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldGetAnalysisById() throws Exception {
        mockMvc.perform(get("/api/analysis/analysis-77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value("analysis-77"))
                .andExpect(jsonPath("$.summary").value("Top candidate: application 401 with score 88.00"));
    }

    private static final class StubAnalysisService implements AnalysisService {

        @Override
        public AnalysisResult startAnalysis(StartAnalysisCommand command) {
            return sampleResult(command.jobId());
        }

        @Override
        public AnalysisResult getAnalysis(String analysisId) {
            return sampleResult("job-77");
        }

        @Override
        public AnalysisResult getReport(String jobId) {
            return sampleResult(jobId);
        }

        @Override
        public List<CandidateAnalysis> getCandidates(String analysisId) {
            return List.of();
        }

        @Override
        public AnalysisResult invalidateByJobId(String jobId) {
            return sampleResult(jobId);
        }

        @Override
        public List<AnalysisResult> invalidateByDocumentId(String documentId) {
            return List.of(sampleResult("job-77"));
        }

        @Override
        public AnalysisResult updateAnalysis(String analysisId, UpdateAnalysisCommand command) {
            return sampleResult("job-77");
        }

        @Override
        public AnalysisResult restartAnalysis(String analysisId) {
            return sampleResult("job-77");
        }

        @Override
        public AnalysisResult updateStatus(String analysisId, String status) {
            return sampleResult("job-77");
        }

        @Override
        public void deleteAnalysis(String analysisId) {
        }

        private AnalysisResult sampleResult(String jobId) {
            return new AnalysisResult(
                    "analysis-77",
                    jobId,
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
