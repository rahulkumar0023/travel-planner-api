package com.axora.travel.service;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class CurrencyService {
  private static final String API_URL = "https://api.exchangerate.host/convert";

  private final RestTemplate rest;

  public CurrencyService(RestTemplateBuilder builder) {
    this.rest = builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
  }

  public double convert(double amount, String from, String to) {
    if (amount <= 0) return 0d;
    if (from == null || to == null) return amount;
    if (from.equalsIgnoreCase(to)) return amount;

    String accessKey = System.getenv("EXCHANGE_API_KEY"); // if you use this API
    URI uri = UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("from", from)
            .queryParam("to", to)
            // ensure dot decimal regardless of device/locale
            .queryParam("amount", String.format(Locale.US, "%.2f", amount))
            .queryParam("access_key", accessKey)
            .build(true).toUri();

    log.info("Calling exchangerate webservice with url: {}", uri);

    try {
      Map<?, ?> res = rest.getForObject(uri, Map.class);
      Object result = (res != null) ? res.get("result") : null;
      if (result instanceof Number n) return n.doubleValue();

      // some responses nest under "info"/"rate" or return error objects
      log.warn("Unexpected response: {}", res);
      return amount; // soft fallback to original value
    } catch (HttpClientErrorException e) {
      log.warn("Convert failed {} -> {} amount {}: {}", from, to, amount, e.getResponseBodyAsString());
      return amount; // soft fallback (avoid 500s)
    } catch (Exception e) {
      log.warn("Convert failed {} -> {} amount {}: {}", from, to, amount, e.toString());
      return amount; // soft fallback
    }
  }
}
