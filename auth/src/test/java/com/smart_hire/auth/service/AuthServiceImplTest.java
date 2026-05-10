package com.smart_hire.auth.service;

import com.smart_hire.auth.domain.UserRole;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.exception.EmailAlreadyExistsException;
import com.smart_hire.auth.repository.UserRepository;
import com.smart_hire.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldFailWhenRegisteringUserWithExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "jane.doe",
                "Password123!",
                "jane.doe@smarthire.dev",
                UserRole.CANDIDATE
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already registered: " + request.email());

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
