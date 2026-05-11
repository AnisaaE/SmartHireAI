package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AuthGatewayController {

    private final GatewayProxyService gatewayProxyService;

    @PostMapping("/api/auth/register")
    ResponseEntity<String> register(@RequestBody String body, HttpServletRequest request) {
        return gatewayProxyService.forwardPost(gatewayProxyService.authBaseUrl(), request.getRequestURI(), body);
    }
}
