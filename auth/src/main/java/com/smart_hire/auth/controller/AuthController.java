package com.smart_hire.auth.controller;

import com.smart_hire.auth.dto.LoginRequest;
import com.smart_hire.auth.dto.LoginResponse;
import com.smart_hire.auth.dto.RegisterRequest;
import com.smart_hire.auth.dto.TokenValidationResponse;
import jakarta.validation.Valid;
import com.smart_hire.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        String token = extractBearerToken(authorizationHeader);
        return ResponseEntity.ok(new TokenValidationResponse(authService.validateToken(token)));
    }

    private String extractBearerToken(String authorizationHeader) {
        return authorizationHeader.replace("Bearer ", "");
    }
}
