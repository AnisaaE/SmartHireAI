package com.smart_hire.dispatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
class SecurityGuardFilter extends OncePerRequestFilter {

    private final RequestAuthorizationSupport requestAuthorizationSupport;

    SecurityGuardFilter(RequestAuthorizationSupport requestAuthorizationSupport) {
        this.requestAuthorizationSupport = requestAuthorizationSupport;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/documents/")
                && !requestAuthorizationSupport.hasBearerToken(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
