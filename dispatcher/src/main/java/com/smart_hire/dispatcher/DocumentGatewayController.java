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

    private final RequestAuthorizationSupport requestAuthorizationSupport;

    @GetMapping("/api/documents/{id}")
    ResponseEntity<Void> getDocument(@PathVariable String id, HttpServletRequest request) {
        if (!requestAuthorizationSupport.hasBearerToken(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
