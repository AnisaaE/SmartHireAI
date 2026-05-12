package com.smart_hire.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.smart_hire.auth.domain.UserRole;

public record UpdateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotNull(message = "Role is required")
        UserRole role,
        boolean active
) {
}
