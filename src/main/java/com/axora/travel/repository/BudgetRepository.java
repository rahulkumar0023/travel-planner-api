package com.axora.travel.repository;

import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetKind;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, String> {
    List<Budget> findByKind(BudgetKind kind);
}
