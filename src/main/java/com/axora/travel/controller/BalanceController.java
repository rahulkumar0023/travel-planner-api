//package com.axora.travel.controller;
//
//import com.axora.travel.entities.Budget;
//import com.axora.travel.entities.BudgetKind;
//import com.axora.travel.repository.BudgetRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import com.axora.travel.dto.TransferDTO;
//import com.axora.travel.repository.TripRepository;
//import com.axora.travel.security.AppPrincipal;
//import com.axora.travel.service.BalanceService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.http.HttpStatus;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import com.axora.travel.security.AppPrincipal;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import java.util.UUID;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/balances")
//@CrossOrigin(origins = "*")
//@Slf4j
//public class BalanceController {
//  private final BalanceService service;
//  private final TripRepository trips;
//  private final BudgetRepository repo;
//
//  public BalanceController(BalanceService service, TripRepository trips) {
//    this.service = service; this.trips = trips;
//  }
//
//  private void assertMember(String tripId, String email) {
//    var t = trips.findById(tripId).orElseThrow();
//    boolean owner = email != null && email.equals(t.getOwner());
//    boolean participant = t.getParticipants() != null && t.getParticipants().contains(email);
//    if (!(owner || participant)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of trip");
//  }
//
//  @GetMapping("/{tripId}")
//  public List<TransferDTO> getBalances(@PathVariable String tripId,
//                                       @AuthenticationPrincipal AppPrincipal me) {
//    log.info("Received request to compute balances for trip {}", tripId);
//    assertMember(tripId, me.email());
//    return service.compute(tripId);
//  }
//
//  // 👇 NEW: internal helper — builds/saves entity with setters
//// createInternal start
//  private Budget createInternal(BudgetController.CreateReq req, String userEmail) {
//    if (req == null || req.kind() == null) {
//      throw new IllegalArgumentException("kind is required");
//    }
//
//    Budget b = new Budget(); // use no-args ctor to avoid ctor-mismatch
//    b.setId(java.util.UUID.randomUUID().toString());
//
//    // Map kind safely (handles MONTHLY/TRIP or monthly/trip)
//    String k = req.kind().trim();
//    try {
//      b.setKind(BudgetKind.valueOf(k.toUpperCase()));
//    } catch (IllegalArgumentException ex) {
//      throw new IllegalArgumentException("Unknown kind: " + k);
//    }
//
//    b.setCurrency(req.currency());
//    if (req.amount() != null) b.setAmount(req.amount());
//    if (req.name()   != null) b.setName(req.name());
//
//    if (b.getKind() == BudgetKind.MONTHLY) {
//      if (req.year()  != null) b.setYear(req.year());
//      if (req.month() != null) b.setMonth(req.month());
//      b.setOwner(userEmail);
//    } else { // TRIP
//      if (req.tripId() == null || req.tripId().isBlank()) {
//        throw new IllegalArgumentException("tripId required for trip budget");
//      }
//      // Optional: assert member if you have the helper available
//      // assertMember(req.tripId(), userEmail);
//      b.setTripId(req.tripId());
//      b.setOwner(userEmail);
//    }
//    return repo.save(b);
//  }
//// createInternal end
//
//  // 👇 MAPPED endpoint (HTTP) — unchanged except calls createInternal
//// mappedCreate start
//  @PostMapping({ "", "/", "/monthly", "/trip" })
//  public ResponseEntity<Budget> create(@RequestBody BudgetController.CreateReq req,
//                                       @AuthenticationPrincipal AppPrincipal me) {
//    Budget saved = createInternal(req, me.email());
//    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(saved);
//  }
//// mappedCreate end
//
//  // 👇 NEW: NON-MAPPED overload — lets BudgetAliases call controller.create(req)
//// nonMappedCreate start
//  public ResponseEntity<Budget> create(BudgetController.CreateReq req) {
//    Authentication a = SecurityContextHolder.getContext().getAuthentication();
//    AppPrincipal me = (a != null && a.getPrincipal() instanceof AppPrincipal p) ? p : null;
//    if (me == null) throw new org.springframework.web.server.ResponseStatusException(
//            org.springframework.http.HttpStatus.UNAUTHORIZED, "no principal");
//    Budget saved = createInternal(req, me.email());
//    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(saved);
//  }
//// nonMappedCreate end
//// ◀︎ BudgetController — PATCH END
//
//}
//
