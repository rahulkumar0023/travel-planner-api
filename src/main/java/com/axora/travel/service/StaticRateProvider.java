package com.axora.travel.service;

import org.springframework.stereotype.Service;

@Service
public class StaticRateProvider implements RateProvider {
  @Override
  public double rate(String from, String to) {
    if (from.equals("EUR") && to.equals("USD")) return 1.1;
    if (from.equals("USD") && to.equals("EUR")) return 0.91;
    return 1.0;
  }
}

