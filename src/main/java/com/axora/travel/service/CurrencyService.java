package com.axora.travel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@Slf4j
public class CurrencyService {
  private static final String API_URL = "https://api.exchangerate.host/convert";

  public double convert(double amount, String from, String to) {
    RestTemplate rest = new RestTemplate();
    String url = API_URL + "?from=" + from + "&to=" + to + "&amount=" + amount
            + "&access_key=" + System.getenv("EXCHANGE_API_KEY");
    log.info("Calling exchangerate webservice with url: {}", url);
    Map<?, ?> res = rest.getForObject(url, Map.class);
    if (res != null && res.get("result") != null) {
      return ((Number) res.get("result")).doubleValue();
    }
    throw new RuntimeException("Conversion failed: " + res);
  }

}
