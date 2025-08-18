package com.axora.travel.controller;

import com.axora.travel.dto.ExpenseCreateRequest;
import com.axora.travel.dto.ExpenseDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.service.ExpenseService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/expenses")
@Slf4j
public class ExpenseController {

  private final ExpenseRepository expenses;
  private final TripRepository trips;
  private final ExpenseService expenseService;

  public ExpenseController(ExpenseRepository expenses, TripRepository trips, ExpenseService expenseService) { this.expenses = expenses;
      this.trips = trips;
      this.expenseService = expenseService;
  }

  // DTOs
  public record CreateExpenseDto(
      String tripId,
      String title,
      BigDecimal amount,
      String category,
      String paidBy,
      Instant date,
      Set<String> sharedWith,
      String currency
  ) {}

  // ===== LEGACY create mapping change start =====
  @Deprecated
  @PostMapping("/legacy") // was: @PostMapping
  public ResponseEntity<Expense> create(@RequestBody CreateExpenseDto dto) {
    var e = new Expense();
    e.setId(UUID.randomUUID().toString());
    e.setTripId(dto.tripId());
    e.setTitle(dto.title());
    e.setAmount(dto.amount());
    e.setCategory(dto.category());
    e.setPaidBy(dto.paidBy());
    e.setDate(dto.date());
    e.setSharedWith(dto.sharedWith()); // ⬅⬅ now compiles
    // In your POST /expenses create method, after building 'e':
    if (dto.currency() != null && !dto.currency().isBlank()) {
      e.setCurrency(dto.currency().toUpperCase());
    } else {
      var trip = trips.findById(dto.tripId()).orElseThrow();
      e.setCurrency(trip.getCurrency()); // default to trip base currency
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(expenses.save(e));
  }

  @GetMapping("/{tripId}")
  public List<Expense> byTrip(@PathVariable String tripId) {
    return expenses.findByTripIdOrderByDateDescCreatedAtDesc(tripId);
  }

  private ExpenseDTO toDTO(Expense e) {
    return new ExpenseDTO(
        e.getId(),
        e.getTripId(),
        e.getTitle(),
        e.getAmount(),
        e.getCategory(),
        e.getDate() == null ? null : LocalDateTime.ofInstant(e.getDate(), ZoneOffset.UTC),
        e.getPaidBy(),
        e.getSharedWith() == null ? Set.of() : new HashSet<>(e.getSharedWith()));
  }
  @PutMapping("/{id}")
  public ResponseEntity<Expense> updateExpensePut(
      @PathVariable String id, @RequestBody ExpenseCreateRequest dto) {
    var e = expenses.findById(id).orElseThrow();
    if (dto.getTripId() != null) e.setTripId(dto.getTripId());
    if (dto.getTitle() != null) e.setTitle(dto.getTitle());
    if (dto.getAmount() != null) e.setAmount(dto.getAmount());
    if (dto.getCategory() != null) e.setCategory(dto.getCategory());
    if (dto.getPaidBy() != null) e.setPaidBy(dto.getPaidBy());
    if (dto.getDate() != null) e.setDate(dto.getDate().atStartOfDay(ZoneOffset.UTC).toInstant());
    if (dto.getSharedWith() != null) e.setSharedWith(dto.getSharedWith());
    if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
      e.setCurrency(dto.getCurrency().toUpperCase());
    }
    return ResponseEntity.ok(expenses.save(e));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Expense> updateExpensePatch(
      @PathVariable String id, @RequestBody ExpenseCreateRequest dto) {
    return updateExpensePut(id, dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
    if (!expenses.existsById(id)) return ResponseEntity.notFound().build();
    expenses.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  // ===== Expenses Controller: create & list additions start =====
  @PostMapping
  public ResponseEntity<ExpenseDTO> createExpense(
      @RequestBody @Valid ExpenseCreateRequest req) {
    ExpenseDTO created = expenseService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping
  public ResponseEntity<List<ExpenseDTO>> listExpenses(
      @RequestParam(required = false) String tripId) {
    if (tripId != null) {
      return ResponseEntity.ok(expenseService.findByTripId(tripId));
    }
    return ResponseEntity.ok(expenseService.findAll());
  }
  // ===== Expenses Controller: create & list additions end =====
}
