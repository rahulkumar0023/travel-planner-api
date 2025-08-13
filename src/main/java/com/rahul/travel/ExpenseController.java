package com.rahul.travel;

import com.rahul.travel.dto.ExpenseDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
  private final ExpenseRepository expenses;

  public ExpenseController(ExpenseRepository expenses) {
    this.expenses = expenses;
  }

  @PostMapping
  public ExpenseDTO create(@RequestBody @Valid ExpenseDTO dto) {
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
    return expenses.findByTripIdOrderByDateDesc(tripId).stream().map(this::toDTO).toList();
  }

  private ExpenseDTO toDTO(Expense e) {
    return new ExpenseDTO(e.getId(), e.getTripId(), e.getTitle(), e.getAmount(),
        e.getCategory(), e.getDate(), e.getPaidBy(), e.getSharedWith());
  }
}

