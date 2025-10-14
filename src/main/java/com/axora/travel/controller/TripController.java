package com.axora.travel.controller;

import com.axora.travel.dto.TripDTO;
import com.axora.travel.entities.Trip;
import com.axora.travel.repository.BudgetRepository;
import com.axora.travel.repository.ExpenseRepository;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.security.AppPrincipal;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping({"/trips", "/api/trips", "/trip", "/api/trip"})
@Validated
@Slf4j
public class TripController {
  private final TripRepository tripRepo;
  private final BudgetRepository budgets;
  private final ExpenseRepository expenses;
  private final byte[] joinSecret;

  public TripController(TripRepository tripRepo, BudgetRepository budgets, ExpenseRepository expenses,
                        @Value("${security.jwt.secret}") String joinSecret) {
    this.tripRepo = tripRepo;
      this.budgets = budgets;
      this.expenses = expenses;
      this.joinSecret = joinSecret == null ? new byte[0] : joinSecret.getBytes(StandardCharsets.UTF_8);
  }

  @PostMapping
  @Transactional
  public ResponseEntity<TripDTO> create(@RequestBody @Valid TripDTO dto,
                        @AuthenticationPrincipal AppPrincipal me,
                        HttpServletRequest request) {
    String authHeader = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
    String authHint = authHeader == null ? "none" : (authHeader.startsWith("Bearer ") ? "bearer-present" : "non-bearer");
    log.info("Trip CREATE: name={} by={} method={} path={} origin={} authHeader={}",
        dto.name(), (me != null ? me.email() : "null"), request.getMethod(), request.getRequestURI(), request.getHeader("Origin"), authHint);
    Trip t = new Trip();
    t.setName(dto.name());
    t.setStartDate(dto.startDate());
    t.setEndDate(dto.endDate());
    t.setCurrency(dto.currency());
    t.setInitialBudget(dto.initialBudget());           // ← DTO is BigDecimal no
    t.setParticipants(dto.participants() == null ? Set.of() : dto.participants().stream().filter(Objects::nonNull).map(String::toLowerCase).collect(java.util.stream.Collectors.toSet()));
    // when writing into Trip entity from DTO (CSV encode)
    if (dto.spendCurrencies() != null) {
      var csv = String.join(",", dto.spendCurrencies());
      t.setSpendCurrencies(csv);
    }
    if (dto.notes() != null) {
      t.setNotes(dto.notes());
    }
    t.setOwner(me.email() == null ? null : me.email().toLowerCase());
    t = tripRepo.saveAndFlush(t);
    log.info("Trip CREATED id={} name={} owner={} currency={} initialBudget={}",
        t.getId(), t.getName(), t.getOwner(), t.getCurrency(), t.getInitialBudget());

    try {
      if (dto.initialBudget() != null && dto.initialBudget().compareTo(java.math.BigDecimal.ZERO) > 0) {
        com.axora.travel.entities.Budget b = new com.axora.travel.entities.Budget();
        b.setId(java.util.UUID.randomUUID().toString());
        b.setKind(com.axora.travel.entities.BudgetKind.trip);
        b.setCurrency(t.getCurrency());
        b.setAmount(dto.initialBudget());
        b.setTripId(t.getId());
        b.setOwner(me.email() == null ? null : me.email().toLowerCase());
        budgets.saveAndFlush(b);
        log.info("Auto-created trip budget {} for trip {} amount={} {}", b.getId(), t.getId(), b.getAmount(), b.getCurrency());
      }
    } catch (Exception e) {
      log.warn("Failed to auto-create trip budget for trip {}: {}", t.getId(), e.getMessage());
    }

    java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
        .fromCurrentRequestUri().path("/{id}").buildAndExpand(t.getId()).toUri();
    return ResponseEntity.created(location).body(toDTO(t));
  }

  @GetMapping
  public List<TripDTO> all(@AuthenticationPrincipal AppPrincipal me) {
    log.info("Trip LIST by={}", (me != null ? me.email() : "null"));
    return tripRepo.findVisibleTo(me.email() == null ? null : me.email().toLowerCase()).stream().map(this::toDTO).toList();
  }

  private TripDTO toDTO(Trip t) {
    List<String> spend = (t.getSpendCurrencies() == null || t.getSpendCurrencies().isBlank())
            ? List.of()
            : Arrays.stream(t.getSpendCurrencies().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
       return new TripDTO(                               // ← match DTO field order
                   t.getId(),
                   t.getName(),
                   t.getStartDate(),
                   t.getEndDate(),
                   t.getCurrency(),
                   t.getInitialBudget(),
                   t.getParticipants(),
                    spend,
                   t.getNotes());
  }

  @PutMapping("/{id}")
  public TripDTO updateTripPut(@PathVariable String id, @RequestBody TripDTO dto) {
    var t = tripRepo.findById(id).orElseThrow();
    if (dto.name() != null) t.setName(dto.name());
    if (dto.startDate() != null) t.setStartDate(dto.startDate());
    if (dto.endDate() != null) t.setEndDate(dto.endDate());
    if (dto.currency() != null) t.setCurrency(dto.currency());
    if (dto.initialBudget() != null) t.setInitialBudget(dto.initialBudget());
    if (dto.participants() != null) t.setParticipants(dto.participants());
    // when updating Trip entity from DTO (CSV encode)
    if (dto.spendCurrencies() != null) {
      var csv = String.join(",", dto.spendCurrencies());
      t.setSpendCurrencies(csv);
    }
    if (dto.notes() != null) {
      t.setNotes(dto.notes());
    }
    t = tripRepo.save(t);
    return toDTO(t);
  }

  @PatchMapping("/{id}")
  public TripDTO updateTripPatch(@PathVariable String id, @RequestBody TripDTO dto) {
    return updateTripPut(id, dto);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<Void> deleteTrip(@PathVariable String id) {
    if (!tripRepo.existsById(id)) return ResponseEntity.notFound().build();

    // 1) Delete child rows that reference this trip (order matters for FK constraints)
    try {
      // budgets: remove any trip budgets tied to this trip
      try {
        budgets.deleteByTripId(id);        // or budgets.deleteAllTripBudgets(id);
      } catch (Exception ignored) { /* repo method name may vary; use your variant */ }

      // expenses: remove all expenses for this trip
      try {
        expenses.deleteByTripId(id);       // or expenses.deleteAllByTripId(id);
      } catch (Exception ignored) { /* repo method name may vary; use your variant */ }

      // 2) Delete the trip itself
      tripRepo.deleteById(id);
      return ResponseEntity.noContent().build();

    } catch (Exception e) {
      // Optional: surface a clearer message than a raw 500
      return ResponseEntity.status(500).build();
    }
  }

  // 👇 NEW: join trip start
  @PostMapping("/{tripId}/join")
  public ResponseEntity<TripDTO> joinTrip(
          @PathVariable String tripId,
          @AuthenticationPrincipal AppPrincipal me,
          @RequestParam String token) {
    var t = tripRepo.findById(tripId).orElseThrow();
    String material = tripId + ":" + (t.getOwner() == null ? "" : t.getOwner());
    String expected;
    try {
      expected = toHex(hmacSha256(joinSecret, material.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
    if (token == null || token.isBlank() || token.length() != expected.length() ||
            !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
      return ResponseEntity.status(403).build();
    }
    t.getParticipants().add(me.email() == null ? null : me.email().toLowerCase());
    var saved = tripRepo.save(t);

    return ResponseEntity.ok(toDTO(saved));
  }
// 👇 NEW: join trip end


  private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    return mac.doFinal(data);
  }

  private static String toHex(byte[] bytes) {
    char[] hexArray = "0123456789abcdef".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

}
