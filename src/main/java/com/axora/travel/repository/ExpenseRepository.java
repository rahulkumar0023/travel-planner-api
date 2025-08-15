package com.axora.travel.repository;

import java.util.List;

import com.axora.travel.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, String> {
  List<Expense> findByTripIdOrderByDateDescCreatedAtDesc(String tripId);
}
