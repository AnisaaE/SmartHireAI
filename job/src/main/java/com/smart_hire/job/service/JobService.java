package com.smart_hire.job.service;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.dto.JobDetailResponse;
import com.smart_hire.job.dto.JobSummaryResponse;
import com.smart_hire.job.dto.UpdateJobRequest;
import com.smart_hire.job.dto.UpdateJobStatusRequest;

import java.util.List;

public interface JobService {

    void createJob(CreateJobRequest request);

    List<JobSummaryResponse> getAllJobs();

    JobDetailResponse getJobById(Long id);

    List<JobSummaryResponse> getJobsByRecruiterId(Long recruiterId);

    JobDetailResponse updateJob(Long id, UpdateJobRequest request);

    JobDetailResponse updateJobStatus(Long id, UpdateJobStatusRequest request);
}
