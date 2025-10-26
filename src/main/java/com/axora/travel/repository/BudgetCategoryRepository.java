package com.axora.travel.repository;

import com.axora.travel.entities.BudgetCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, String> {
    @EntityGraph(attributePaths = "children")
    List<BudgetCategory> findByMonthlyBudget_IdAndParentIsNullOrderByDisplayOrderAscNameAsc(String monthlyBudgetId);

    List<BudgetCategory> findByParent_IdOrderByDisplayOrderAscNameAsc(String parentId);

    Optional<BudgetCategory> findByIdAndMonthlyBudgetOwner(String id, String owner);
}
