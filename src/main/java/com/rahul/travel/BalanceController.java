package com.rahul.travel;

import com.rahul.travel.dto.GroupBalanceDTO;
import com.rahul.travel.service.SplitService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/balances")
@Slf4j
public class BalanceController {
  private final TripRepository trips;
  private final ExpenseRepository expenses;
  private final SplitService split;

  public BalanceController(TripRepository trips, ExpenseRepository expenses, SplitService split) {
    this.trips = trips;
    this.expenses = expenses;
    this.split = split;
  }

  @GetMapping("/{tripId}")
  public List<GroupBalanceDTO> compute(@PathVariable String tripId) {
    log.info("Received request to compute balances for trip {}", tripId);
    Trip t = trips.findById(tripId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    List<Expense> ex = expenses.findByTripIdOrderByDateDesc(tripId);
    return split.computeBalances(t, ex);
  }
}

