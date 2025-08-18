package com.axora.travel.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ExpenseCreateRequest {
    @NotNull private String tripId;
    @NotNull private BigDecimal amount;
    @NotBlank private String currency;   // e.g., "EUR"
    @NotBlank private String title;
    private String notes;
    private LocalDate date;
    private String category;
    private String paidBy;
    private Set<String> sharedWith;
}

