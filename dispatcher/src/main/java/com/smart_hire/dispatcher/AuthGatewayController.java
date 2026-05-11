package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequiredArgsConstructor
class AuthGatewayController {

    private final DispatcherServiceProperties serviceProperties;

    @PostMapping("/api/auth/register")
    ResponseEntity<String> register(@RequestBody String body, HttpServletRequest request) {
        RestClient restClient = RestClient.builder()
                .baseUrl(serviceProperties.authUrl())
                .build();
        ResponseEntity<String> response = restClient.post()
                .uri(request.getRequestURI())
                .body(body)
                .retrieve()
                .toEntity(String.class);

        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode().value()))
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
