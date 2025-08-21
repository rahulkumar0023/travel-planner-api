package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.repository.BudgetRepository;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.security.AppPrincipal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

// 1) IMPORTS (add if missing) — place with other imports at the top:
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.axora.travel.security.AppPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.UUID;

@RestController
@CrossOrigin // or configure CORS globally
// Support both plain and /api prefix, and singular endpoints your client may try
@RequestMapping({"/budgets", "/api/budgets"})
@Slf4j
public class BudgetController {
  private final BudgetRepository repo;
  private final TripRepository trips;
  public BudgetController(BudgetRepository repo, TripRepository trips) { this.repo = repo; this.trips = trips; }

  @GetMapping
  public List<Budget> all(@AuthenticationPrincipal AppPrincipal me) {
    log.info("Balance request by user={}", me != null ? me.email() : "null");
    return repo.findByOwner(me.email()); }

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

  // 4) (Optional) membership helper if you check trip budgets:
  private void assertMember(String tripId, String email) {
    var t = trips.findById(tripId).orElseThrow();
    boolean owner = email != null && email.equalsIgnoreCase(t.getOwner());
    boolean participant = t.getParticipants() != null && t.getParticipants().contains(email);

    // allow access to orphan trips in dev to prevent lock-out
    boolean orphan = (t.getOwner() == null || t.getOwner().isBlank())
        && (t.getParticipants() == null || t.getParticipants().isEmpty());
    if (!(owner || participant || orphan)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of trip");
    }
  }

  // 5) INTERNAL CREATION (setter-based to avoid ctor mismatch)
  private Budget createInternal(CreateReq req, String userEmail) {
    if (req == null || req.kind() == null || req.kind().isBlank()) {
      throw new IllegalArgumentException("kind is required");
    }

    Budget b = new Budget();                // no-args JPA ctor
    b.setId(UUID.randomUUID().toString());  // if your entity auto-generates, you can remove this

    // Kind mapping — adapt if your enum is lowercase/uppercase
    String k = req.kind().trim().toUpperCase(); // e.g., "MONTHLY" or "TRIP"
    b.setKind(BudgetKind.valueOf(k));

    b.setCurrency(req.currency());
    if (req.amount() != null) b.setAmount(req.amount());
    if (req.name()   != null) b.setName(req.name());

    if (b.getKind() == BudgetKind.monthly) {
      if (req.year()  != null) b.setYear(req.year());
      if (req.month() != null) b.setMonth(req.month());
      b.setOwner(userEmail);
    } else { // TRIP
      if (req.tripId() == null || req.tripId().isBlank()) {
        throw new IllegalArgumentException("tripId required for trip budget");
      }
      // assertMember(req.tripId(), userEmail); // enable if you injected TripRepository
      b.setTripId(req.tripId());
      b.setOwner(userEmail); // optional
    }
    return repo.save(b);
  }

  // 6) MAPPED HTTP ENDPOINT (used by the app)
  @PostMapping({ "", "/", "/monthly", "/trip" })
  public ResponseEntity<Budget> create(@RequestBody CreateReq req,
                                       @AuthenticationPrincipal AppPrincipal me) {
    log.info("Balance request by user={} for trip={}", me != null ? me.email() : "null", req.tripId);
    Budget saved = createInternal(req, me.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  // 7) NON-MAPPED OVERLOAD (used by BudgetAliases calling controller directly)
  public ResponseEntity<Budget> create(CreateReq req) {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    AppPrincipal me = (a != null && a.getPrincipal() instanceof AppPrincipal p) ? p : null;
    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no principal");
    Budget saved = createInternal(req, me.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

// ===== BudgetController — PATCH END =====


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
