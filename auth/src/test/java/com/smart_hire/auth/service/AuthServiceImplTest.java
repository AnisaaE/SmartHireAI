package com.smart_hire.auth.service;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UserResponse;
import com.smart_hire.auth.exception.UsernameAlreadyExistsException;
import com.smart_hire.auth.repository.UserRepository;
import com.smart_hire.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldFailWhenRegisteringUserWithExistingUsername() {
        RegisterRequest request = new RegisterRequest(
                "jane.doe",
                "Password123!"
        );

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already registered: " + request.username());

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(7L).username("jane.doe").password("encoded-1").build(),
                User.builder().id(8L).username("john.doe").password("encoded-2").build()
        ));

        List<UserResponse> users = authService.getAllUsers();

        assertThat(users).containsExactly(
                new UserResponse(7L, "jane.doe"),
                new UserResponse(8L, "john.doe")
        );
        verify(userRepository).findAll();
    }
}
