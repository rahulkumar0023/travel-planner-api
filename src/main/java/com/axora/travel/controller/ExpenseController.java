package com.axora.travel.controller;

import com.axora.travel.dto.ExpenseDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/expenses")
@Slf4j
public class ExpenseController {
  private final ExpenseRepository expenses;

  public ExpenseController(ExpenseRepository expenses) {
    this.expenses = expenses;
  }

  @PostMapping
  public ExpenseDTO create(@RequestBody @Valid ExpenseDTO dto) {
    log.info("Received request to create expense for trip {}", dto.tripId());
    Expense e = new Expense();
    e.setTripId(dto.tripId());
    e.setTitle(dto.title());
    e.setAmount(dto.amount());
    e.setCategory(dto.category());
    e.setDate(dto.date());
    e.setPaidBy(dto.paidBy());
    e.setSharedWith(dto.sharedWith() == null ? Set.of() : dto.sharedWith());
    e = expenses.save(e);
    return toDTO(e);
  }

  @GetMapping("/{tripId}")
  public List<ExpenseDTO> byTrip(@PathVariable String tripId) {
    log.info("Received request to list expenses for trip {}", tripId);
    return expenses.findByTripIdOrderByDateDesc(tripId).stream().map(this::toDTO).toList();
  }

  private ExpenseDTO toDTO(Expense e) {
    return new ExpenseDTO(e.getId(), e.getTripId(), e.getTitle(), e.getAmount(),
        e.getCategory(), e.getDate(), e.getPaidBy(), e.getSharedWith());
  }
}

