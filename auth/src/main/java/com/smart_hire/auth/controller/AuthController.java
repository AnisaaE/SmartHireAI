package com.smart_hire.auth.controller;

import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.TokenValidationResponse;
import com.smart_hire.auth.dto.UpdateUserRequest;
import com.smart_hire.auth.dto.UserResponse;
import jakarta.validation.Valid;
import com.smart_hire.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping(AuthApiPaths.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(AuthApiPaths.REGISTER_PATH)
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping(AuthApiPaths.LOGIN_PATH)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping(AuthApiPaths.VALIDATE_PATH)
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return ResponseEntity.ok(new TokenValidationResponse(
                authService.validateToken(extractTokenFromHeader(authorizationHeader))
        ));
    }

    @GetMapping(AuthApiPaths.USER_BY_ID_PATH)
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PutMapping(AuthApiPaths.USER_BY_ID_PATH)
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @DeleteMapping(AuthApiPaths.USER_BY_ID_PATH)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        return authorizationHeader.replace("Bearer ", "");
    }
}
