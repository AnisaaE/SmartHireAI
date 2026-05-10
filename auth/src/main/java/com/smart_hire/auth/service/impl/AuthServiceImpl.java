package com.smart_hire.auth.service.impl;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.exception.UsernameAlreadyExistsException;
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

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        return userRepository.save(user);
    }

    private void validateUsernameAvailability(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already registered: " + username);
        }
    }
}
