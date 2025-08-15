package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.repository.BudgetRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/budgets")
@CrossOrigin
public class BudgetController {
  private final BudgetRepository repo;
  public BudgetController(BudgetRepository repo) { this.repo = repo; }

  @GetMapping
  public List<Budget> all() { return repo.findAll(); }

  record CreateReq(String kind, String currency, BigDecimal amount,
                   Integer year, Integer month, String tripId, String name) {}

  @PostMapping
  public Budget create(@RequestBody CreateReq req) {
    var b = new Budget(UUID.randomUUID().toString(),
        BudgetKind.valueOf(req.kind), req.currency, req.amount);
    b.setYear(req.year); b.setMonth(req.month);
    b.setTripId(req.tripId); b.setName(req.name);
    return repo.save(b);
  }

  record LinkReq(String monthlyBudgetId) {}

  @PostMapping("/{tripBudgetId}/link")
  public Budget link(@PathVariable String tripBudgetId, @RequestBody LinkReq body) {
    var trip = repo.findById(tripBudgetId).orElseThrow();
    trip.setLinkedMonthlyBudgetId(body.monthlyBudgetId());
    return repo.save(trip);
  }
}
