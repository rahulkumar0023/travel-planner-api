package com.axora.travel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyService {
  private final RestTemplate http = new RestTemplate();
  private final Map<String, Cache> cache = new ConcurrentHashMap<>();

  record Cache(double rate, long ts) {}

  public double convert(String from, String to, double amount) {
    if (from.equalsIgnoreCase(to)) return amount;
    String key = (from + "_" + to).toUpperCase();
    Cache c = cache.get(key);
    long now = Instant.now().getEpochSecond();
    if (c == null || now - c.ts > 3600) {
      Map<?, ?> m = http.getForObject(
          "https://api.exchangerate.host/latest?base={from}&symbols={to}",
          Map.class,
          Map.of("from", from, "to", to));
      double rate = ((Map<String, Double>) m.get("rates")).get(to.toUpperCase());
      c = new Cache(rate, now);
      cache.put(key, c);
    }
    return amount * c.rate;
  }
}

