package com.smart_hire.auth.dto;

import com.smart_hire.auth.domain.UserRole;

public record RegisterRequest(
        String username,
        String password,
        String email,
        UserRole role
) {
}
