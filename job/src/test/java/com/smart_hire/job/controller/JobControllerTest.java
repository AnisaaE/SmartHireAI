package com.smart_hire.job.controller;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void shouldReturnCreatedWhenCreateJobPayloadIsValid() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recruiterId": 5,
                                  "title": "Senior Java Developer",
                                  "description": "Build backend services",
                                  "location": "Istanbul",
                                  "employmentType": "FULL_TIME"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(jobService).createJob(any(CreateJobRequest.class));
    }
}
