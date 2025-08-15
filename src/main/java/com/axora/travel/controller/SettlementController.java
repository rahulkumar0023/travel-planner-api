package com.axora.travel.controller;

import com.axora.travel.entities.Settlement;
import com.axora.travel.repository.SettlementRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/balances")
@CrossOrigin
public class SettlementController {
  private final SettlementRepository repo;
  public SettlementController(SettlementRepository repo) { this.repo = repo; }

  // matches POST /balances/settle your app already calls
  record SettleReq(String tripId, String from, String to,
                   BigDecimal amount, String currency, String note) {}

  @PostMapping("/settle")
  public void settle(@RequestBody SettleReq req) {
    var s = new Settlement();
    s.setId(UUID.randomUUID().toString());
    s.setTripId(req.tripId()); s.setPayer(req.from()); s.setPayee(req.to());
    s.setAmount(req.amount()); s.setCurrency(req.currency()); s.setNote(req.note());
    repo.save(s);
  }
}
