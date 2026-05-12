package com.smart_hire.auth.service;

import com.smart_hire.auth.domain.User;
import com.smart_hire.auth.domain.UserRole;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UserResponse;
import com.smart_hire.auth.exception.UsernameAlreadyExistsException;
import com.smart_hire.auth.repository.UserRepository;
import com.smart_hire.auth.service.AuthJobClient;
import com.smart_hire.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

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

    @Mock
    private AuthJobClient authJobClient;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldFailWhenRegisteringUserWithExistingUsername() {
        RegisterRequest request = new RegisterRequest(
                "jane.doe",
                "jane@example.com",
                "Password123!",
                UserRole.CANDIDATE
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
                User.builder().id(7L).username("jane.doe").email("jane@example.com").password("encoded-1").role(UserRole.CANDIDATE).active(true).build(),
                User.builder().id(8L).username("john.doe").email("john@example.com").password("encoded-2").role(UserRole.RECRUITER).active(false).build()
        ));

        List<UserResponse> users = authService.getAllUsers();

        assertThat(users).containsExactly(
                new UserResponse(7L, "jane.doe", "jane@example.com", UserRole.CANDIDATE, true),
                new UserResponse(8L, "john.doe", "john@example.com", UserRole.RECRUITER, false)
        );
        verify(userRepository).findAll();
    }

    @Test
    void shouldDeactivateRecruiterAndArchiveRecruiterJobs() {
        User recruiter = User.builder()
                .id(8L)
                .username("john.doe")
                .email("john@example.com")
                .password("encoded")
                .role(UserRole.RECRUITER)
                .active(true)
                .build();
        when(userRepository.findById(8L)).thenReturn(Optional.of(recruiter));
        when(authJobClient.getJobsByRecruiterId(8L)).thenReturn(List.of(
                new AuthJobClient.JobSummary(11L, 8L, "Backend Engineer", "OPEN"),
                new AuthJobClient.JobSummary(12L, 8L, "QA Engineer", "ARCHIVED")
        ));

        authService.deleteUserById(8L);

        verify(authJobClient).archiveJob(11L);
        verify(userRepository).save(recruiter);
        assertThat(recruiter.isActive()).isFalse();
    }
}
