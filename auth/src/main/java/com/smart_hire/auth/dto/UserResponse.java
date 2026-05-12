package com.smart_hire.auth.dto;

import com.smart_hire.auth.domain.UserRole;

public record UserResponse(Long id, String username, String email, UserRole role, boolean active) {
}
