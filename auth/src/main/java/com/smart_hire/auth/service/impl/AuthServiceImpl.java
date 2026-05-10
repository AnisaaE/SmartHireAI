package com.smart_hire.auth.service.impl;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UserResponse;
import com.smart_hire.auth.dto.UpdateUserRequest;
import com.smart_hire.auth.exception.UsernameAlreadyExistsException;
import com.smart_hire.auth.exception.UserNotFoundException;
import com.smart_hire.auth.repository.UserRepository;
import com.smart_hire.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {
        validateUsernameAvailability(request.username());
        return userRepository.save(buildUser(request));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return new LoginResponse(issueAccessToken(request.username()));
    }

    @Override
    public boolean validateToken(String token) {
        return "jwt-token-value".equals(token);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.updateUsername(request.username());
        return mapToUserResponse(userRepository.save(user));
    }

    private void validateUsernameAvailability(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already registered: " + username);
        }
    }

    private User buildUser(RegisterRequest request) {
        return User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();
    }

    private String issueAccessToken(String username) {
        return "jwt-token-value";
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
