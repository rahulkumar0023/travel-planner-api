package com.axora.travel.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> onValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    log.warn("Validation {} {} -> 400 (origin={}): {}", req.getMethod(), req.getRequestURI(), req.getHeader("Origin"), ex.getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "validation_failed");
    body.put("details", ex.getBindingResult().getFieldErrors().stream()
        .map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage()))
        .toList());
    return body;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> onConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
    log.warn("Constraint {} {} -> 400 (origin={}): {}", req.getMethod(), req.getRequestURI(), req.getHeader("Origin"), ex.getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "validation_failed");
    body.put("details", ex.getConstraintViolations().stream()
        .map(v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()))
        .toList());
    return body;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> onMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.warn("Malformed JSON {} {} -> 400 (origin={}): {}", req.getMethod(), req.getRequestURI(), req.getHeader("Origin"), ex.getMostSpecificCause().getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "malformed_json");
    body.put("message", ex.getMostSpecificCause().getMessage());
    return body;
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> onResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    log.warn("RSE {} {} -> {} {} (origin={}): {}",
        req.getMethod(), req.getRequestURI(), status.value(), status.getReasonPhrase(), req.getHeader("Origin"), ex.getReason());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", status.getReasonPhrase());
    body.put("message", ex.getReason());
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> onGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled {} {} -> 500 (origin={})", req.getMethod(), req.getRequestURI(), req.getHeader("Origin"), ex);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "internal_error");
    body.put("message", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
