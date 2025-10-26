package com.axora.travel.dto;

import com.axora.travel.entities.BudgetCategoryType;
import java.math.BigDecimal;

public record BudgetSubcategoryDTO(
    String id,
    String name,
    BudgetCategoryType type,
    BigDecimal plannedAmount,
    BigDecimal actualAmount,
    Integer displayOrder
) {}
