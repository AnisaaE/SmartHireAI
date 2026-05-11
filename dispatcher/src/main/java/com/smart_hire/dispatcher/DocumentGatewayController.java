package com.smart_hire.dispatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class DocumentGatewayController {

    static final String DOCUMENT_BY_ID_PATH = "/api/documents/{id}";

    private final RequestAuthorizationSupport requestAuthorizationSupport;

    @GetMapping(DOCUMENT_BY_ID_PATH)
    ResponseEntity<Void> getDocument(@PathVariable String id, HttpServletRequest request) {
        if (!requestAuthorizationSupport.hasBearerToken(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
