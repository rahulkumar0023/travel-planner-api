package com.axora.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record ExpenseDTO(
    String id,
    @NotBlank String tripId,
    String title,
    @NotNull BigDecimal amount,  // ← BigDecimal
    String category,
    LocalDateTime date,
    @NotBlank String paidBy,
    Set<String> sharedWith,
    String currency,
    Set<String> tags         // 👈 NEW
) {}
