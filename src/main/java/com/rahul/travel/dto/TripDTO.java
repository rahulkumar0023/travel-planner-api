package com.rahul.travel.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;

public record TripDTO(
    String id,
    @NotBlank String name,
    LocalDate startDate,
    LocalDate endDate,
    Double initialBudget,
    String currency,
    Set<String> participants) {}

