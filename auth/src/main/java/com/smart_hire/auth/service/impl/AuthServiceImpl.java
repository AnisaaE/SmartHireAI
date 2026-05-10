package com.smart_hire.auth.service.impl;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.exception.EmailAlreadyExistsException;
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
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(request.role())
                .build();

        return userRepository.save(user);
    }
}
