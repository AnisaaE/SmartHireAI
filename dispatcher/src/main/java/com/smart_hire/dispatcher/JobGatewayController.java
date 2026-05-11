package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class JobGatewayController {

    private final GatewayProxyService gatewayProxyService;

    @GetMapping("/api/jobs")
    ResponseEntity<String> getJobs(HttpServletRequest request) {
        return gatewayProxyService.forwardGet(gatewayProxyService.jobBaseUrl(), request.getRequestURI());
    }
}
