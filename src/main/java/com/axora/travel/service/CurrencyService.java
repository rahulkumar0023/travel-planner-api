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
    if (from == null || to == null) throw new IllegalArgumentException("from/to required");
    final String f = from.trim().toUpperCase();
    final String t = to.trim().toUpperCase();
    if (f.equals(t)) return amount;

    final String key = f + "_" + t;
    final long now = Instant.now().getEpochSecond();
    Cache c = cache.get(key);

    if (c == null || now - c.ts > 3600) { // 1h TTL
      Map<?,?> m;
      try {
        // Ask for conversion of "1" unit so result == exchange rate
        m = http.getForObject(
            "https://api.exchangerate.host/convert?from={from}&to={to}&amount=1",
            Map.class,
            Map.of("from", f, "to", t));
      } catch (RestClientException e) {
        throw new RuntimeException("upstream rates fetch failed", e);
      }

      if (m == null || Boolean.FALSE.equals(m.get("success"))) {
        throw new IllegalArgumentException("invalid currency code or upstream error");
      }

      Object result = m.get("result"); // rate for 1 unit
      BigDecimal rate;
      if (result instanceof Number n) {
        rate = BigDecimal.valueOf(n.doubleValue());
      } else if (result instanceof String s) {
        rate = new BigDecimal(s);
      } else {
        throw new IllegalStateException("unexpected payload from exchangerate.host");
      }

      c = new Cache(rate, now);
      cache.put(key, c);
    }

    return BigDecimal.valueOf(amount).multiply(c.rate).doubleValue();
  }
}