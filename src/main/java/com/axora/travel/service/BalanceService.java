package com.axora.travel.service;

import com.axora.travel.dto.TransferDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class BalanceService {
  private final ExpenseRepository repo;

  public BalanceService(ExpenseRepository repo) {
    this.repo = repo;
  }

  public List<TransferDTO> compute(String tripId) {
    List<Expense> expenses = repo.findByTripIdOrderByDateDesc(tripId);
    Map<String, BigDecimal> net = new HashMap<>();

    for (Expense e : expenses) {
      var participants = e.getSharedWith();
      if (participants == null || participants.isEmpty()) continue;

      BigDecimal amount = e.getAmount();
      BigDecimal perHead =
          amount.divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);

      net.merge(e.getPaidBy(), amount, BigDecimal::add);
      for (String p : participants) {
        net.merge(p, perHead.negate(), BigDecimal::add);
      }
    }

    PriorityQueue<Map.Entry<String, BigDecimal>> debtors =
        new PriorityQueue<>(Map.Entry.comparingByValue());
    PriorityQueue<Map.Entry<String, BigDecimal>> creditors =
        new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));

    BigDecimal threshold = new BigDecimal("0.01");
    for (var entry : net.entrySet()) {
      BigDecimal v = entry.getValue().setScale(2, RoundingMode.HALF_UP);
      if (v.abs().compareTo(threshold) < 0) continue;
      var e = Map.entry(entry.getKey(), v);
      if (v.signum() < 0) debtors.add(e); else creditors.add(e);
    }

    List<TransferDTO> transfers = new ArrayList<>();
    while (!debtors.isEmpty() && !creditors.isEmpty()) {
      var d = debtors.poll();
      var c = creditors.poll();
      BigDecimal pay = d.getValue().negate().min(c.getValue());
      pay = pay.setScale(2, RoundingMode.HALF_UP);
      transfers.add(new TransferDTO(d.getKey(), c.getKey(), pay));
      BigDecimal dRemain = d.getValue().add(pay).setScale(2, RoundingMode.HALF_UP);
      BigDecimal cRemain = c.getValue().subtract(pay).setScale(2, RoundingMode.HALF_UP);
      if (dRemain.compareTo(threshold.negate()) < 0) debtors.add(Map.entry(d.getKey(), dRemain));
      if (cRemain.compareTo(threshold) > 0) creditors.add(Map.entry(c.getKey(), cRemain));
    }
    return transfers;
  }
}

