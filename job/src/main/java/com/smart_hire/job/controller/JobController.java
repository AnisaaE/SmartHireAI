package com.smart_hire.job.controller;

import com.smart_hire.job.dto.CreateJobRequest;
import com.smart_hire.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Void> createJob(@Valid @RequestBody CreateJobRequest request) {
        jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
