package com.axora.travel.controller;

import com.axora.travel.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/currency")
@Slf4j
public class CurrencyController {
  private final CurrencyService service;
  public CurrencyController(CurrencyService service) { this.service = service; }

  @GetMapping("/convert")
  public ResponseEntity<Map<String, Object>> convert(
      @RequestParam String from,
      @RequestParam String to,
      @RequestParam double amount) {
    log.info("Received request to convert {} from {} to {}", amount, from, to);
    try {
      double out = service.convert(from, to, amount);
      return ResponseEntity.ok(Map.of(
          "from", from.toUpperCase(),
          "to", to.toUpperCase(),
          "amount", out));
    } catch (IllegalArgumentException e) {
      log.warn("Bad params: {}", e.getMessage());
      return ResponseEntity.badRequest().body(Map.of(
          "error", "invalid_currency_or_params",
          "message", e.getMessage()));
    } catch (Exception e) {
      log.error("Provider failed", e);
      return ResponseEntity.status(502).body(Map.of(
          "error", "upstream_failed",
          "message", "Currency provider error"));
    }
  }
}