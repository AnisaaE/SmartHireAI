package com.smart_hire.application.controller;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.ApplicationSummaryResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationRequest;
import com.smart_hire.application.dto.UpdateApplicationStatusRequest;
import com.smart_hire.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Test
    void shouldReturnCreatedWhenApplyPayloadIsValid() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobId": 12,
                                  "candidateId": 34,
                                  "cvDocumentId": "cv-doc-101"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(applicationService).apply(any(CreateApplicationRequest.class));
    }

    @Test
    void shouldReturnApplicationDetailsWhenApplicationExists() throws Exception {
        doReturn(new ApplicationDetailResponse(
                7L,
                12L,
                34L,
                "cv-doc-101",
                "APPLIED"
        )).when(applicationService).getApplicationById(7L);

        mockMvc.perform(get("/api/applications/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.jobId").value(12))
                .andExpect(jsonPath("$.candidateId").value(34))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(applicationService).getApplicationById(7L);
    }

    @Test
    void shouldReturnApplicationsForJobWhenJobHasApplications() throws Exception {
        doReturn(List.of(
                new ApplicationSummaryResponse(7L, 12L, 34L, "APPLIED"),
                new ApplicationSummaryResponse(8L, 12L, 55L, "UNDER_REVIEW")
        )).when(applicationService).getApplicationsByJobId(12L);

        mockMvc.perform(get("/api/applications/job/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].jobId").value(12))
                .andExpect(jsonPath("$[0].candidateId").value(34))
                .andExpect(jsonPath("$[0].status").value("APPLIED"))
                .andExpect(jsonPath("$[1].id").value(8))
                .andExpect(jsonPath("$[1].candidateId").value(55))
                .andExpect(jsonPath("$[1].status").value("UNDER_REVIEW"));

        verify(applicationService).getApplicationsByJobId(12L);
    }

    @Test
    void shouldReturnApplicationsForCandidateWhenCandidateHasApplications() throws Exception {
        doReturn(List.of(
                new ApplicationSummaryResponse(7L, 12L, 34L, "APPLIED"),
                new ApplicationSummaryResponse(9L, 18L, 34L, "SHORTLISTED")
        )).when(applicationService).getApplicationsByCandidateId(34L);

        mockMvc.perform(get("/api/applications/candidate/34"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].jobId").value(12))
                .andExpect(jsonPath("$[0].candidateId").value(34))
                .andExpect(jsonPath("$[0].status").value("APPLIED"))
                .andExpect(jsonPath("$[1].id").value(9))
                .andExpect(jsonPath("$[1].jobId").value(18))
                .andExpect(jsonPath("$[1].status").value("SHORTLISTED"));

        verify(applicationService).getApplicationsByCandidateId(34L);
    }

    @Test
    void shouldReturnUpdatedApplicationWhenUpdatePayloadIsValid() throws Exception {
        doReturn(new ApplicationDetailResponse(
                7L,
                12L,
                34L,
                "cv-doc-202",
                "APPLIED"
        )).when(applicationService).updateApplication(7L, new UpdateApplicationRequest(
                "cv-doc-202"
        ));

        mockMvc.perform(put("/api/applications/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cvDocumentId": "cv-doc-202"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.cvDocumentId").value("cv-doc-202"))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(applicationService).updateApplication(7L, new UpdateApplicationRequest("cv-doc-202"));
    }

    @Test
    void shouldReturnUpdatedApplicationWhenStatusPayloadIsValid() throws Exception {
        doReturn(new ApplicationDetailResponse(
                7L,
                12L,
                34L,
                "cv-doc-202",
                "UNDER_REVIEW"
        )).when(applicationService).updateApplicationStatus(7L, new UpdateApplicationStatusRequest(
                "UNDER_REVIEW"
        ));

        mockMvc.perform(put("/api/applications/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "UNDER_REVIEW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        verify(applicationService).updateApplicationStatus(7L, new UpdateApplicationStatusRequest("UNDER_REVIEW"));
    }
}
