package com.rahul.travel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, String> {
  List<Expense> findByTripIdOrderByDateDesc(String tripId);
}
