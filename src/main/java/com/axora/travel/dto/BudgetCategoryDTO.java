package com.axora.travel.dto;

import com.axora.travel.entities.BudgetCategoryType;
import java.math.BigDecimal;
import java.util.List;

public record BudgetCategoryDTO(
    String id,
    String name,
    BudgetCategoryType type,
    BigDecimal plannedAmount,
    BigDecimal actualAmount,
    Integer displayOrder,
    List<BudgetSubcategoryDTO> subcategories
) {}
