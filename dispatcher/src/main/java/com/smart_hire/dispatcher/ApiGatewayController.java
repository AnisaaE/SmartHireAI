package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
class ApiGatewayController {

    private final GatewayProxyService gatewayProxyService;

    @RequestMapping({
            "/api/auth/**",
            "/api/documents/**",
            "/api/jobs/**",
            "/api/applications/**",
            "/api/analysis/**"
    })
    ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        return gatewayProxyService.forward(request);
    }
}
