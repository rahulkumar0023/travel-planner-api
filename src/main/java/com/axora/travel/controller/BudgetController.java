package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.repository.BudgetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@CrossOrigin // or configure CORS globally
// Support both plain and /api prefix, and singular endpoints your client may try
@RequestMapping({"/budgets", "/api/budgets"})
public class BudgetController {
  private final BudgetRepository repo;
  public BudgetController(BudgetRepository repo) { this.repo = repo; }

  @GetMapping
  public List<Budget> all() { return repo.findAll(); }

  record CreateReq(String kind, String currency, BigDecimal amount,
                   Integer year, Integer month, String tripId, String name) {}

  // Accept POST /budgets, /budgets/monthly, /budgets/trip
  @PostMapping({ "", "/", "/monthly", "/trip" })
  public ResponseEntity<Budget> create(@RequestBody CreateReq req) {
    var id = UUID.randomUUID().toString();
    var b = new Budget(id,
            BudgetKind.valueOf(req.kind()), // expects "monthly" or "trip"
            req.currency(), req.amount());
    b.setYear(req.year()); b.setMonth(req.month());
    b.setTripId(req.tripId()); b.setName(req.name());
    return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(b));
  }
  record LinkReq(String monthlyBudgetId) {}

  @PostMapping("/{tripBudgetId}/link")
  public Budget link(@PathVariable String tripBudgetId, @RequestBody LinkReq body) {
    var trip = repo.findById(tripBudgetId).orElseThrow();
    trip.setLinkedMonthlyBudgetId(body.monthlyBudgetId());
    return repo.save(trip);
  }
}
