// src/main/java/com/axora/travel/dto/TripDTO.java
package com.axora.travel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record TripDTO(
        String id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String currency,
        BigDecimal initialBudget,     // ← BigDecimal to match DB/entity
        Set<String> participants
) {}
