package com.rahul.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Set;

public record ExpenseDTO(
    String id,
    @NotBlank String tripId,
    String title,
    @NotNull @Positive Double amount,
    String category,
    LocalDateTime date,
    @NotBlank String paidBy,
    Set<String> sharedWith) {}

