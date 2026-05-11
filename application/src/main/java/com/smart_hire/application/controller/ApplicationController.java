package com.smart_hire.application.controller;

import com.smart_hire.application.dto.ApplicationDetailResponse;
import com.smart_hire.application.dto.ApplicationSummaryResponse;
import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApplicationApiPaths.BASE_PATH)
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Void> apply(@Valid @RequestBody CreateApplicationRequest request) {
        applicationService.apply(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(ApplicationApiPaths.APPLICATION_BY_ID_PATH)
    public ResponseEntity<ApplicationDetailResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationSummaryResponse>> getApplicationsByJobId(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId));
    }
}
