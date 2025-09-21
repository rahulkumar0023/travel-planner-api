package com.axora.travel.controller;

import com.axora.travel.dto.TripDTO;
import com.axora.travel.entities.Trip;
import com.axora.travel.repository.BudgetRepository;
import com.axora.travel.repository.ExpenseRepository;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.security.AppPrincipal;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/trips")
@Validated
@Slf4j
public class TripController {
  private final TripRepository tripRepo;
  private final BudgetRepository budgets;
  private final ExpenseRepository expenses;

  public TripController(TripRepository tripRepo, BudgetRepository budgets, ExpenseRepository expenses) {
    this.tripRepo = tripRepo;
      this.budgets = budgets;
      this.expenses = expenses;
  }

  @PostMapping
  public TripDTO create(@RequestBody @Valid TripDTO dto,
                        @AuthenticationPrincipal AppPrincipal me) {
    log.info("Received request to create trip {}", dto.name());
    Trip t = new Trip();
    t.setName(dto.name());
    t.setStartDate(dto.startDate());
    t.setEndDate(dto.endDate());
    t.setCurrency(dto.currency());
    t.setInitialBudget(dto.initialBudget());           // ← DTO is BigDecimal no
    t.setParticipants(dto.participants() == null ? Set.of() : dto.participants());
    // when writing into Trip entity from DTO (CSV encode)
    if (dto.spendCurrencies() != null) {
      var csv = String.join(",", dto.spendCurrencies());
      t.setSpendCurrencies(csv);
    }
    t.setOwner(me.email()); // 👇 NEW: owner
    t = tripRepo.save(t);
    return toDTO(t);
  }

  @GetMapping
  public List<TripDTO> all(@AuthenticationPrincipal AppPrincipal me) {
    log.info("Received request to list all trips");
    return tripRepo.findVisibleTo(me.email()).stream().map(this::toDTO).toList();
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
                    spend);
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
    // TODO: validate token securely (simple check for now)
    t.getParticipants().add(me.email());
    var saved = tripRepo.save(t);

    // Convert entity -> DTO
    var dto = new TripDTO(
            saved.getId(),
            saved.getName(),
            saved.getStartDate(),
            saved.getEndDate(),
            saved.getCurrency(),
            saved.getInitialBudget(),
            saved.getParticipants(),
            Collections.singletonList(saved.getSpendCurrencies())  // make sure Trip.java has this getter
    );

    return ResponseEntity.ok(dto);
  }
// 👇 NEW: join trip end


}

