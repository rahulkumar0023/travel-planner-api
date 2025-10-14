package com.axora.travel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

// Budget.java

@Getter
@Setter
@Entity @Table(name = "budgets")
public class Budget {
  @Id private String id;
  @Enumerated(EnumType.STRING) private BudgetKind kind;
  private String currency;
  @Column(precision = 12, scale = 2) private BigDecimal amount;
  private Integer year; private Integer month;
  @Column(name = "trip_id") private String tripId;
  private String name;
  @Column(name = "linked_monthly_budget_id") private String linkedMonthlyBudgetId;

  // --- owner field start ---
  @Column(name = "owner")
  private String owner;
  // --- owner field end ---

  @JsonIgnore
  @OneToMany(mappedBy = "monthlyBudget", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("displayOrder ASC, name ASC")
  private Set<BudgetCategory> categories = new LinkedHashSet<>();

  public Budget() {}
  public Budget(String id, BudgetKind kind, String currency, BigDecimal amount) {
    this.id = id; this.kind = kind; this.currency = currency; this.amount = amount;
  }
  // getters/setters…
}
