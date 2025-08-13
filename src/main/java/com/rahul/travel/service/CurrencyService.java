package com.rahul.travel.service;

import org.springframework.stereotype.Service;

@Service
public class CurrencyService {
  private final RateProvider rateProvider;

  public CurrencyService(RateProvider rateProvider) {
    this.rateProvider = rateProvider;
  }

  public double convert(double amount, String from, String to) {
    if (from.equalsIgnoreCase(to)) {
      return amount;
    }
    return amount * rateProvider.rate(from, to);
  }
}

