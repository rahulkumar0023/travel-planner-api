package com.axora.travel.controller;

import com.axora.travel.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/currency")
@Slf4j
public class CurrencyController {
  private final CurrencyService service;

  public CurrencyController(CurrencyService service) {
    this.service = service;
  }

  @GetMapping("/convert")
  public Map<String, Object> convert(@RequestParam String from, @RequestParam String to,
                                     @RequestParam double amount) {
    log.info("Received request to convert {} from {} to {}", amount, from, to);
    double result = service.convert(from, to, amount);
    return Map.of("from", from.toUpperCase(), "to", to.toUpperCase(), "amount", result);
  }
}

