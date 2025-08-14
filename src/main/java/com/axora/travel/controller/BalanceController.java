package com.axora.travel.controller;

import com.axora.travel.dto.TransferDTO;
import com.axora.travel.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/balances")
@CrossOrigin(origins = "*")
@Slf4j
public class BalanceController {
  private final BalanceService service;

  public BalanceController(BalanceService service) {
    this.service = service;
  }

  @GetMapping("/{tripId}")
  public List<TransferDTO> getBalances(@PathVariable String tripId) {
    log.info("Received request to compute balances for trip {}", tripId);
    return service.compute(tripId);
  }
}

