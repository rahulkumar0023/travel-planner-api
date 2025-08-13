package com.rahul.travel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, String> {
  List<Expense> findByTripId(String tripId);
}
