package com.smart_hire.job.controller;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.dto.JobDetailResponse;
import com.smart_hire.job.dto.JobSummaryResponse;
import com.smart_hire.job.dto.UpdateJobRequest;
import com.smart_hire.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(JobApiPaths.BASE_PATH)
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Void> createJob(@Valid @RequestBody CreateJobRequest request) {
        jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<JobSummaryResponse>> getAllJobs() {
        List<JobSummaryResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping(JobApiPaths.JOB_BY_ID_PATH)
    public ResponseEntity<JobDetailResponse> getJobById(@PathVariable Long id) {
        JobDetailResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping(JobApiPaths.JOBS_BY_RECRUITER_PATH)
    public ResponseEntity<List<JobSummaryResponse>> getJobsByRecruiterId(@PathVariable Long recruiterId) {
        List<JobSummaryResponse> jobs = jobService.getJobsByRecruiterId(recruiterId);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping(JobApiPaths.JOB_BY_ID_PATH)
    public ResponseEntity<JobDetailResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }
}
