package com.rahul.travel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
  @Autowired private ExpenseRepository repo;

  @PostMapping
  public Expense save(@RequestBody Expense exp) {
    return repo.save(exp);
  }

  @GetMapping("/{tripId}")
  public List<Expense> getByTrip(@PathVariable String tripId) {
    return repo.findByTripId(tripId);
  }
}
