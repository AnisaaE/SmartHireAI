package com.smart_hire.auth.controller;

import com.smart_hire.auth.config.SecurityConfig;
import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.UpdateUserRequest;
import com.smart_hire.auth.dto.UserResponse;
import com.smart_hire.auth.exception.InvalidCredentialsException;
import com.smart_hire.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void shouldReturnCreatedWhenRegisterRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane.doe",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestMissesPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane.doe"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturnTokenWhenLoginRequestIsValid() throws Exception {
        doReturn(new LoginResponse("jwt-token-value"))
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane.doe",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnValidResponseWhenTokenCanBeValidated() throws Exception {
        doReturn(true)
                .when(authService)
                .validateToken("jwt-token-value");

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer jwt-token-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        verify(authService).validateToken(eq("jwt-token-value"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        doThrow(new InvalidCredentialsException("Invalid username or password"))
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane.doe",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void shouldReturnForbiddenWhenValidateEndpointRejectsTokenAccess() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Forbidden"))
                .when(authService)
                .validateToken("blocked-token");

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer blocked-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        doReturn(new UserResponse(7L, "jane.doe"))
                .when(authService)
                .getUserById(7L);

        mockMvc.perform(get("/api/auth/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("jane.doe"));

        verify(authService).getUserById(7L);
    }

    @Test
    void shouldUpdateUsernameWhenAuthenticatedUserSendsValidPayload() throws Exception {
        doReturn(new UserResponse(7L, "jane.updated"))
                .when(authService)
                .updateUser(7L, new UpdateUserRequest("jane.updated"));

        mockMvc.perform(put("/api/auth/users/7")
                        .with(user("jane.doe"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "jane.updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("jane.updated"));

        verify(authService).updateUser(7L, new UpdateUserRequest("jane.updated"));
    }
}
