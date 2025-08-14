package com.axora.travel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class CurrencyService {
    private static final String API_URL = "https://api.exchangerate.host/convert";

    public double convert(double amount, String from, String to) {
        RestTemplate rest = new RestTemplate();
        String url = API_URL + "?from=" + from + "&to=" + to + "&amount=" + amount;
        Map<String, Object> res = rest.getForObject(url, Map.class);
        if (res != null && res.containsKey("result")) {
            return ((Number) res.get("result")).doubleValue();
        }
        throw new RuntimeException("Conversion failed");
    }
}
