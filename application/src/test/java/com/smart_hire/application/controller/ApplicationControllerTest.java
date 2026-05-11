package com.smart_hire.application.controller;

import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
