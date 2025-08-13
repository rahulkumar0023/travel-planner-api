package com.rahul.travel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
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

  @GetMapping("/split/{tripId}")
  public List<GroupBalance> splitTrip(@PathVariable String tripId) {
    List<Expense> expenses = repo.findByTripId(tripId);
    Map<String, Map<String, Double>> balanceSheet = new HashMap<>();

    for (Expense e : expenses) {
      int n = e.getSharedWith().size();
      double share = e.getAmount() / n;
      for (String user : e.getSharedWith()) {
        if (!user.equals(e.getPaidBy())) {
          balanceSheet.putIfAbsent(user, new HashMap<>());
          Map<String, Double> owesTo = balanceSheet.get(user);

          owesTo.put(e.getPaidBy(), owesTo.getOrDefault(e.getPaidBy(), 0.0) + share);
        }
      }
    }

    List<GroupBalance> results = new ArrayList<>();
    for (String user : balanceSheet.keySet()) {
      GroupBalance gb = new GroupBalance();
      gb.setUser(user);
      gb.setOwes(balanceSheet.get(user));
      results.add(gb);
    }

    return results;
  }
}
