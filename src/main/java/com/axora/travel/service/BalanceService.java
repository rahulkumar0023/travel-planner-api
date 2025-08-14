package com.axora.travel.service;

import com.axora.travel.dto.TransferDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BalanceService {
  private final ExpenseRepository repo;

  public BalanceService(ExpenseRepository repo) {
    this.repo = repo;
  }

  public List<TransferDTO> compute(String tripId) {
    List<Expense> expenses = repo.findByTripIdOrderByDateDesc(tripId);
    Map<String, Double> net = new HashMap<>();

    for (Expense e : expenses) {
      var participants = e.getSharedWith();
      if (participants == null || participants.isEmpty()) continue;
      double perHead = e.getAmount() / participants.size();

      net.merge(e.getPaidBy(), e.getAmount(), Double::sum);
      for (String p : participants) {
        net.merge(p, -perHead, Double::sum);
      }
    }

    PriorityQueue<Map.Entry<String, Double>> debtors =
        new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
    PriorityQueue<Map.Entry<String, Double>> creditors =
        new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));

    for (var entry : net.entrySet()) {
      double v = Math.round(entry.getValue() * 100.0) / 100.0;
      if (Math.abs(v) < 0.01) continue;
      var e = Map.entry(entry.getKey(), v);
      if (v < 0) debtors.add(e); else creditors.add(e);
    }

    List<TransferDTO> transfers = new ArrayList<>();
    while (!debtors.isEmpty() && !creditors.isEmpty()) {
      var d = debtors.poll();
      var c = creditors.poll();
      double pay = Math.min(-d.getValue(), c.getValue());
      transfers.add(new TransferDTO(d.getKey(), c.getKey(), Math.round(pay * 100.0) / 100.0));
      double dRemain = d.getValue() + pay;
      double cRemain = c.getValue() - pay;
      if (dRemain < -0.01) debtors.add(Map.entry(d.getKey(), dRemain));
      if (cRemain > 0.01) creditors.add(Map.entry(c.getKey(), cRemain));
    }
    return transfers;
  }
}

