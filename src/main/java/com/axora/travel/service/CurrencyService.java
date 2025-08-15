package com.axora.travel.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyService {
  private final RestTemplate http;
  private final Map<String, Cache> cache = new ConcurrentHashMap<>();

  record Cache(BigDecimal rate, long ts) {}

  public CurrencyService() {
    this.http = new RestTemplate();
    var f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(5000);
    f.setReadTimeout(5000);
    this.http.setRequestFactory(f);
  }

  public double convert(String from, String to, double amount) {
    if (from == null || to == null || from.isBlank() || to.isBlank()) {
      throw new IllegalArgumentException("from/to required");
    }
    final String f = from.trim().toUpperCase();
    final String t = to.trim().toUpperCase();
    if (f.equals(t)) return amount;

    final String key = f + "_" + t;
    final long now = Instant.now().getEpochSecond();
    Cache c = cache.get(key);

    if (c == null || now - c.ts > 3600) { // 1h TTL
      BigDecimal rate = fetchRate(f, t); // may throw RuntimeException on provider failure
      c = new Cache(rate, now);
      cache.put(key, c);
    }

    return BigDecimal.valueOf(amount).multiply(c.rate).doubleValue();
  }

  private BigDecimal fetchRate(String f, String t) {
    // 1) Primary: exchangerate.host /convert (result = rate for amount=1)
    try {
      Map<?,?> m = http.getForObject(
          "https://api.exchangerate.host/convert?from={from}&to={to}&amount=1",
          Map.class,
          Map.of("from", f, "to", t));
      if (m != null) {
        Object result = m.get("result");
        if (result instanceof Number n) {
          double val = n.doubleValue();
          if (val > 0) return BigDecimal.valueOf(val);
        } else if (result instanceof String s) {
          BigDecimal bd = new BigDecimal(s);
          if (bd.signum() > 0) return bd;
        }
      }
    } catch (Exception ignored) {
      // fall through to fallback
    }

    // 2) Fallback: Frankfurter API
    try {
      Map<?,?> m2 = http.getForObject(
          "https://api.frankfurter.app/latest?from={from}&to={to}",
          Map.class,
          Map.of("from", f, "to", t));
      if (m2 != null) {
        Object ratesObj = m2.get("rates");
        if (ratesObj instanceof Map<?,?> rates) {
          Object v = rates.get(t);
          if (v instanceof Number n) {
            double val = n.doubleValue();
            if (val > 0) return BigDecimal.valueOf(val);
          } else if (v instanceof String s) {
            BigDecimal bd = new BigDecimal(s);
            if (bd.signum() > 0) return bd;
          }
        }
      }
    } catch (Exception ignored) {
      // no-op
    }

    throw new RuntimeException("FX lookup failed for " + f + "→" + t);
  }
}