package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.entities.Trip;
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
import jakarta.servlet.http.HttpServletRequest;

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
    String user = me == null ? null : (me.email() == null ? null : me.email().toLowerCase());
    return repo.findByOwner(user); }

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
  private Trip assertMember(String tripId, String email) {
    // Small tolerance for immediate follow-up requests right after trip creation
    // in case the DB commit hasn't propagated yet.
    Trip found = null;
    for (int i = 0; i < 5; i++) {
      var optTry = trips.findById(tripId);
      if (optTry.isPresent()) { found = optTry.get(); break; }
      try { Thread.sleep(60L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
    }
    var opt = java.util.Optional.ofNullable(found);
    if (opt.isEmpty()) {
      // Extra diagnostics: show what the user CAN see to catch ID mismatches/races
      try {
        var visible = trips.findVisibleTo(email);
        var ids = visible.stream().map(Trip::getId).limit(5).toList();
        log.warn("Trip not found: id={} user={} visible_count={} sample_ids={}", tripId, email, visible.size(), ids);
      } catch (Exception e) {
        log.warn("Trip not found and failed to fetch visible list: id={} user={} err={}", tripId, email, e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip with ID " + tripId + " not found");
    }
    var t = opt.get();
    boolean owner = email != null && email.equalsIgnoreCase(t.getOwner());
    boolean participant = t.getParticipants() != null && t.getParticipants().contains(email);

    // allow access to orphan trips in dev to prevent lock-out
    boolean orphan = (t.getOwner() == null || t.getOwner().isBlank())
        && (t.getParticipants() == null || t.getParticipants().isEmpty());
    if (!(owner || participant || orphan)) {
      log.warn("Trip access denied: id={} user={} owner={} participants={}", tripId, email, t.getOwner(), t.getParticipants());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of trip");
    }
    return t;
  }

  // 5) INTERNAL CREATION (setter-based to avoid ctor mismatch)
  private Budget createInternal(CreateReq req, String userEmail) {
    if (req == null || req.kind() == null || req.kind().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind is required");
    }

    Budget b = new Budget();                // no-args JPA ctor
    b.setId(UUID.randomUUID().toString());  // if your entity auto-generates, you can remove this

    // Kind mapping — enum values are lowercase in BudgetKind
    String k = req.kind().trim().toLowerCase(); // accept MONTHLY/TRIP/etc.
    try {
      b.setKind(BudgetKind.valueOf(k));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid kind: " + req.kind());
    }

    b.setCurrency(req.currency());
    if (req.amount() != null) b.setAmount(req.amount());
    if (req.name()   != null) b.setName(req.name());

    if (b.getKind() == BudgetKind.monthly) {
      if (req.year()  != null) b.setYear(req.year());
      if (req.month() != null) b.setMonth(req.month());
      b.setOwner(userEmail);
    } else { // TRIP
      if (req.tripId() == null || req.tripId().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId required for trip budget");
      }
      var trip = assertMember(req.tripId(), userEmail);
      b.setTripId(req.tripId());
      b.setOwner(userEmail); // optional
      // default currency to the trip's currency if none provided
      if (b.getCurrency() == null || b.getCurrency().isBlank()) {
        b.setCurrency(trip.getCurrency());
      }
    }
    return repo.saveAndFlush(b);
  }

  // 6) MAPPED HTTP ENDPOINT (used by the app)
  @PostMapping({ "", "/", "/monthly", "/trip" })
  public ResponseEntity<Budget> create(@RequestBody CreateReq req,
                                       @AuthenticationPrincipal AppPrincipal me,
                                       @RequestParam(value = "tripId", required = false) String tripIdParam,
                                       HttpServletRequest httpReq) {
    if (me == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no principal");
    }
    // Allow clients to pass tripId via query param when body omits it
    if ((req.tripId() == null || req.tripId().isBlank()) && tripIdParam != null && !tripIdParam.isBlank()) {
      req = new CreateReq(
          req.kind(),
          req.currency(),
          req.amount(),
          req.year(),
          req.month(),
          tripIdParam,
          req.name()
      );
    }

    log.info("Budget create by user={} path={} tripId={} kind={}", me.email(), httpReq.getRequestURI(), req.tripId(), req.kind());
    String user = me.email() == null ? null : me.email().toLowerCase();
    Budget saved = createInternal(req, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  // 7) NON-MAPPED OVERLOAD (used by BudgetAliases calling controller directly)
  public ResponseEntity<Budget> create(CreateReq req) {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    AppPrincipal me = (a != null && a.getPrincipal() instanceof AppPrincipal p) ? p : null;
    if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no principal");
    String user = me.email() == null ? null : me.email().toLowerCase();
    Budget saved = createInternal(req, user);
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
    if (req.kind() != null) {
      try {
        b.setKind(BudgetKind.valueOf(req.kind().toLowerCase()));
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid kind: " + req.kind());
      }
    }
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
