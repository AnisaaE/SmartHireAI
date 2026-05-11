package com.smart_hire.ai_analysis.api;

import com.smart_hire.ai_analysis.service.StartAnalysisCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StartAnalysisRequest(
        @NotBlank(message = "must not be blank")
        String jobId,
        @NotBlank(message = "must not be blank")
        String jobTitle,
        @NotBlank(message = "must not be blank")
        String jobDescription,
        @NotEmpty(message = "must not be empty")
        List<@Valid ApplicationSnapshotRequest> applications,
        @Valid
        @NotNull(message = "must not be null")
        AnalysisConfigurationRequest configuration
) {

    public StartAnalysisCommand toCommand() {
        return new StartAnalysisCommand(
                jobId,
                jobTitle,
                applications.stream().map(ApplicationSnapshotRequest::toSnapshot).toList(),
                configuration.toConfiguration(),
                jobDescription
        );
    }
}
