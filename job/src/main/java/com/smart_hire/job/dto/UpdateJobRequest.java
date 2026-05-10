package com.smart_hire.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateJobRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,
        @NotBlank(message = "Description is required")
        String description,
        @NotBlank(message = "Location is required")
        String location,
        @NotBlank(message = "Employment type is required")
        String employmentType
) {
}
