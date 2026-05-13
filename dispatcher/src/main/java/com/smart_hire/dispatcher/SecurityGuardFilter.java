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
        if (requiresBearerToken(request) && !requestAuthorizationSupport.hasBearerToken(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresBearerToken(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (!uri.startsWith("/api/")) {
            return false;
        }

        if (isPublicAuthEndpoint(uri) || isPublicJobRead(request, uri)) {
            return false;
        }

        return true;
    }

    private boolean isPublicAuthEndpoint(String uri) {
        return "/api/auth/register".equals(uri) || "/api/auth/login".equals(uri);
    }

    private boolean isPublicJobRead(HttpServletRequest request, String uri) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        return "/api/jobs".equals(uri) || uri.matches("^/api/jobs/[^/]+$");
    }
}
