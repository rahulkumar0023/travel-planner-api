package com.axora.travel.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Logs details when unauthenticated requests are rejected with 401.
 */
public class LoggingAuthEntryPoint implements AuthenticationEntryPoint {
  private static final Logger log = LoggerFactory.getLogger(LoggingAuthEntryPoint.class);

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
    String method = request.getMethod();
    String path = request.getRequestURI();
    String origin = request.getHeader("Origin");
    String authHeader = request.getHeader("Authorization");
    String authHint = authHeader == null ? "none" : (authHeader.startsWith("Bearer ") ? "bearer-present" : "non-bearer");
    log.warn("401 Unauthorized: method={} path={} origin={} authHeader={} reason={}",
        method, path, origin, authHint,
        (authException == null ? "unknown" : authException.getMessage()));

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}

