package com.smart_hire.auth.service.impl;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.domain.UserRole;
import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UserResponse;
import com.smart_hire.auth.dto.UpdateUserRequest;
import com.smart_hire.auth.exception.EmailAlreadyExistsException;
import com.smart_hire.auth.exception.InvalidCredentialsException;
import com.smart_hire.auth.exception.UsernameAlreadyExistsException;
import com.smart_hire.auth.exception.UserNotFoundException;
import com.smart_hire.auth.repository.UserRepository;
import com.smart_hire.auth.service.AuthService;
import com.smart_hire.auth.service.AuthJobClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJobClient authJobClient;

    @Override
    public User register(RegisterRequest request) {
        validateUsernameAvailability(request.username());
        validateEmailAvailability(request.email());
        return userRepository.save(buildUser(request));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return new LoginResponse(issueAccessToken(request.username()));
    }

    @Override
    public boolean validateToken(String token) {
        return "jwt-token-value".equals(token);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapToUserResponse(getExistingUser(id));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getExistingUser(id);
        if (!user.getUsername().equals(request.username())) {
            validateUsernameAvailability(request.username());
        }
        if (!user.getEmail().equals(request.email())) {
            validateEmailAvailability(request.email());
        }
        user.updateProfile(request.username(), request.email(), request.role(), request.active());
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUserById(Long id) {
        deactivateUser(getExistingUser(id));
    }

    private void validateUsernameAvailability(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already registered: " + username);
        }
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered: " + email);
        }
    }

    private User buildUser(RegisterRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build();
    }

    private String issueAccessToken(String username) {
        return "jwt-token-value";
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isActive());
    }

    private User getExistingUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    private void deactivateUser(User user) {
        archiveRecruiterJobs(user);
        user.updateProfile(user.getUsername(), user.getEmail(), user.getRole(), false);
        userRepository.save(user);
    }

    private void archiveRecruiterJobs(User user) {
        if (user.getRole() != UserRole.RECRUITER) {
            return;
        }
        authJobClient.getJobsByRecruiterId(user.getId()).stream()
                .filter(job -> !"ARCHIVED".equalsIgnoreCase(job.status()))
                .forEach(job -> authJobClient.archiveJob(job.id()));
    }
}
