package com.smart_hire.auth.service;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.RegisterRequest;

public interface AuthService {

    User register(RegisterRequest request);
}
