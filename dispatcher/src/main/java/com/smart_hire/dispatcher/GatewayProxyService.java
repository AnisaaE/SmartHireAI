package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
class GatewayProxyService {

    private static final Set<String> REQUEST_HEADERS_TO_SKIP = Set.of(
            HttpHeaders.HOST.toLowerCase(Locale.ROOT),
            HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.ROOT)
    );

    private static final Set<String> RESPONSE_HEADERS_TO_SKIP = Set.of(
            HttpHeaders.TRANSFER_ENCODING.toLowerCase(Locale.ROOT),
            HttpHeaders.CONNECTION.toLowerCase(Locale.ROOT)
    );

    private final DispatcherServiceProperties serviceProperties;
    private final RestClient restClient = RestClient.builder().build();

    ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException {
        String baseUrl = resolveBaseUrl(request.getRequestURI());
        URI targetUri = URI.create(baseUrl + request.getRequestURI() + querySuffix(request));
        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());

        return restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUri)
                .headers(headers -> copyRequestHeaders(request, headers))
                .body(requestBody)
                .exchange((clientRequest, clientResponse) -> {
                    HttpHeaders responseHeaders = new HttpHeaders();
                    clientResponse.getHeaders().forEach((name, values) -> {
                        if (!RESPONSE_HEADERS_TO_SKIP.contains(name.toLowerCase(Locale.ROOT))) {
                            responseHeaders.put(name, values);
                        }
                    });
                    byte[] responseBody = StreamUtils.copyToByteArray(clientResponse.getBody());
                    return ResponseEntity
                            .status(clientResponse.getStatusCode())
                            .headers(responseHeaders)
                            .body(responseBody);
                });
    }

    private String resolveBaseUrl(String requestUri) {
        if (requestUri.startsWith("/api/auth")) {
            return serviceProperties.authUrl();
        }
        if (requestUri.startsWith("/api/documents")) {
            return serviceProperties.documentUrl();
        }
        if (requestUri.startsWith("/api/jobs")) {
            return serviceProperties.jobUrl();
        }
        if (requestUri.startsWith("/api/applications")) {
            return serviceProperties.applicationUrl();
        }
        if (requestUri.startsWith("/api/analysis")) {
            return serviceProperties.analysisUrl();
        }
        throw new IllegalArgumentException("Unsupported route: " + requestUri);
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpHeaders headers) {
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (REQUEST_HEADERS_TO_SKIP.contains(headerName.toLowerCase(Locale.ROOT))) {
                return;
            }
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        });
    }

    private String querySuffix(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString == null || queryString.isBlank() ? "" : "?" + queryString;
    }
}
