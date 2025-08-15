package com.axora.travel.controller;

import com.axora.travel.dto.ExpenseDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
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

  private final ExpenseRepository repo;
  public ExpenseController(ExpenseRepository repo) { this.repo = repo; }

  // DTOs
  public record CreateExpenseDto(
      String tripId,
      String title,
      BigDecimal amount,
      String category,
      String paidBy,
      Instant date,
      Set<String> sharedWith
  ) {}

  @PostMapping
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
    return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
  }

  @GetMapping("/{tripId}")
  public List<Expense> byTrip(@PathVariable String tripId) {
    return repo.findByTripIdOrderByDateDescCreatedAtDesc(tripId);
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
}

