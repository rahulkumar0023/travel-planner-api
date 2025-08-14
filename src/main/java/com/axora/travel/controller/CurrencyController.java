package com.axora.travel.controller;

import com.axora.travel.service.CurrencyService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/currency")
public class CurrencyController {
    private final CurrencyService currency;
    public CurrencyController(CurrencyService currency) { this.currency = currency; }

    @GetMapping("/convert")
    public Map<String, Object> convert(
            @RequestParam double amount,
            @RequestParam String from,
            @RequestParam String to
    ) {
        double result = currency.convert(amount, from, to);
        return Map.of("amount", result);
    }
}
