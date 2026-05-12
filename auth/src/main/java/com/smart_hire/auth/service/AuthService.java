package com.smart_hire.auth.service;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UpdateUserRequest;
import com.smart_hire.auth.dto.UserResponse;

import java.util.List;

public interface AuthService {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    boolean validateToken(String token);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUserById(Long id);
}
