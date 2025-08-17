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

  // --- update() start ---
  @PutMapping("/{id}")
  @PatchMapping("/{id}")
  public ResponseEntity<Budget> update(@PathVariable String id, @RequestBody CreateReq req) {
    var b = repo.findById(id).orElseThrow();
    if (req.kind() != null) b.setKind(BudgetKind.valueOf(req.kind()));
    if (req.currency() != null) b.setCurrency(req.currency());
    if (req.amount() != null) b.setAmount(req.amount());
    if (req.year() != null) b.setYear(req.year());
    if (req.month() != null) b.setMonth(req.month());
    if (req.tripId() != null) b.setTripId(req.tripId());
    if (req.name() != null) b.setName(req.name());
    return ResponseEntity.ok(repo.save(b));
  }
// --- update() end ---

  // --- delete() start ---
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  // Some clients POST to /budgets/{id}/delete; support that too
  @PostMapping("/{id}/delete")
  public ResponseEntity<Void> deletePost(@PathVariable String id) {
    return delete(id);
  }
// --- delete() end ---

  // --- unlink() start ---
  @PostMapping("/{tripBudgetId}/unlink")
  public ResponseEntity<Budget> unlink(@PathVariable String tripBudgetId) {
    var t = repo.findById(tripBudgetId).orElseThrow();
    t.setLinkedMonthlyBudgetId(null);
    return ResponseEntity.ok(repo.save(t));
  }
// --- unlink() end ---
}
