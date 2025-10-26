package com.axora.travel.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Logs details when authenticated requests are forbidden with 403.
 */
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {
  private static final Logger log = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    String method = request.getMethod();
    String path = request.getRequestURI();
    String origin = request.getHeader("Origin");
    log.warn("403 Forbidden: method={} path={} origin={} reason={}",
        method, path, origin, accessDeniedException.getMessage());
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
}

