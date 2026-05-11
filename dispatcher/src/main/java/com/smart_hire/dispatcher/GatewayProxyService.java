package com.smart_hire.dispatcher;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
class GatewayProxyService {

    private final DispatcherServiceProperties serviceProperties;

    String authBaseUrl() {
        return serviceProperties.authUrl();
    }

    String jobBaseUrl() {
        return serviceProperties.jobUrl();
    }

    ResponseEntity<String> forwardPost(String baseUrl, String path, String body) {
        ResponseEntity<String> response = RestClient.builder()
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(path)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode().value()))
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    ResponseEntity<String> forwardGet(String baseUrl, String path) {
        ResponseEntity<String> response = RestClient.builder()
                .baseUrl(baseUrl)
                .build()
                .get()
                .uri(path)
                .retrieve()
                .toEntity(String.class);

        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode().value()))
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
