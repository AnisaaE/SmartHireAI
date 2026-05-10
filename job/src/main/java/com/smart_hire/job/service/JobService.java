package com.smart_hire.job.service;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.dto.JobSummaryResponse;

import java.util.List;

public interface JobService {

    void createJob(CreateJobRequest request);

    List<JobSummaryResponse> getAllJobs();
}
