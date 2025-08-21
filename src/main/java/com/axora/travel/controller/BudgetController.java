package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.repository.BudgetRepository;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.security.AppPrincipal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@CrossOrigin // or configure CORS globally
// Support both plain and /api prefix, and singular endpoints your client may try
@RequestMapping({"/budgets", "/api/budgets"})
public class BudgetController {
  private final BudgetRepository repo;
  private final TripRepository trips;
  public BudgetController(BudgetRepository repo, TripRepository trips) { this.repo = repo; this.trips = trips; }

  @GetMapping
  public List<Budget> all(@AuthenticationPrincipal AppPrincipal me) { return repo.findByOwner(me.email()); }

  // annotate the record
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static record CreateReq(
          String kind,          // "monthly" | "trip"
          String currency,      // e.g., "EUR"
          BigDecimal amount,    // optional
          Integer year,         // monthly-only
          Integer month,        // monthly-only
          String tripId,        // trip-budget-only
          String name           // optional label
  ) {}

  private void assertMember(String tripId, String email) {
    var t = trips.findById(tripId).orElseThrow();
    boolean owner = email != null && email.equals(t.getOwner());
    boolean participant = t.getParticipants() != null && t.getParticipants().contains(email);
    if (!(owner || participant)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of trip");
  }

  // Accept POST /budgets, /budgets/monthly, /budgets/trip
  @PostMapping({ "", "/", "/monthly", "/trip" })
  public ResponseEntity<Budget> create(@RequestBody CreateReq req,
                                       @AuthenticationPrincipal AppPrincipal me) {
    if (req == null || req.kind() == null) {
      return ResponseEntity.badRequest().build();
    }

    // Guard trip-budget calls
    if ("trip".equalsIgnoreCase(req.kind())) {
      if (req.tripId() == null || req.tripId().isBlank()) {
        return ResponseEntity.badRequest().build();
      }
      assertMember(req.tripId(), me.email());
    }

    // Build using setters to avoid ctor mismatch
    Budget b = new Budget();
    b.setId(UUID.randomUUID().toString());
    b.setKind(BudgetKind.valueOf(req.kind().toLowerCase()));
    b.setCurrency(req.currency());
    if (req.amount() != null)  b.setAmount(req.amount());
    if (req.name()   != null)  b.setName(req.name());

    if ("monthly".equalsIgnoreCase(req.kind())) {
      if (req.year() != null)  b.setYear(req.year());
      if (req.month()!= null)  b.setMonth(req.month());
      b.setOwner(me.email());
    } else {
      b.setTripId(req.tripId());
      b.setOwner(me.email());
    }

    Budget saved = repo.save(b);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
  record LinkReq(String monthlyBudgetId) {}

  @PostMapping("/{tripBudgetId}/link")
  public Budget link(@PathVariable String tripBudgetId, @RequestBody LinkReq body) {
    var trip = repo.findById(tripBudgetId).orElseThrow();
    trip.setLinkedMonthlyBudgetId(body.monthlyBudgetId());
    return repo.save(trip);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Budget> updatePut(@PathVariable String id, @RequestBody CreateReq req) {
    var b = repo.findById(id).orElseThrow();
    if (req.kind() != null) b.setKind(BudgetKind.valueOf(req.kind().toLowerCase()));
    if (req.currency() != null) b.setCurrency(req.currency());
    if (req.amount() != null) b.setAmount(req.amount());
    if (req.year() != null) b.setYear(req.year());
    if (req.month() != null) b.setMonth(req.month());
    if (req.tripId() != null) b.setTripId(req.tripId());
    if (req.name() != null) b.setName(req.name());
    return ResponseEntity.ok(repo.save(b));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Budget> updatePatch(@PathVariable String id, @RequestBody CreateReq req) {
    return updatePut(id, req);
  }

//  // --- update() start ---
//  @PutMapping("/{id}")
//  @PatchMapping("/{id}")
//  public ResponseEntity<Budget> update(@PathVariable String id, @RequestBody CreateReq req) {
//    var b = repo.findById(id).orElseThrow();
//    if (req.kind() != null) b.setKind(BudgetKind.valueOf(req.kind()));
//    if (req.currency() != null) b.setCurrency(req.currency());
//    if (req.amount() != null) b.setAmount(req.amount());
//    if (req.year() != null) b.setYear(req.year());
//    if (req.month() != null) b.setMonth(req.month());
//    if (req.tripId() != null) b.setTripId(req.tripId());
//    if (req.name() != null) b.setName(req.name());
//    return ResponseEntity.ok(repo.save(b));
//  }
//// --- update() end ---

@PostMapping("/{id}")
public ResponseEntity<Budget> updatePost(@PathVariable String id, @RequestBody CreateReq req) {
  return updatePut(id, req);
}

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/delete")
  public ResponseEntity<Void> deletePost(@PathVariable String id) {
    return delete(id);
  }

  @PostMapping("/{tripBudgetId}/unlink")
  public ResponseEntity<Budget> unlink(@PathVariable String tripBudgetId) {
    var t = repo.findById(tripBudgetId).orElseThrow();
    t.setLinkedMonthlyBudgetId(null);
    return ResponseEntity.ok(repo.save(t));
  }
}
