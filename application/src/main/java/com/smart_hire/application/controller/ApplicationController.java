package com.smart_hire.application.controller;

import com.smart_hire.application.dto.CreateApplicationRequest;
import com.smart_hire.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Void> apply(@Valid @RequestBody CreateApplicationRequest request) {
        applicationService.apply(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
