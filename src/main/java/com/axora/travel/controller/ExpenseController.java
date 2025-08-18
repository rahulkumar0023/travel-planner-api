package com.axora.travel.controller;

import com.axora.travel.dto.ExpenseCreateRequest;
import com.axora.travel.dto.ExpenseDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import com.axora.travel.service.ExpenseService;
import jakarta.validation.Valid;
import java.time.ZoneOffset;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expenses")
@Slf4j
public class ExpenseController {

  private final ExpenseRepository expenses;
  private final ExpenseService expenseService;

  public ExpenseController(ExpenseRepository expenses, ExpenseService expenseService) {
    this.expenses = expenses;
    this.expenseService = expenseService;
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
