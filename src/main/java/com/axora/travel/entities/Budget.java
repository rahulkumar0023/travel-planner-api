package com.axora.travel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

// Budget.java

@Getter
@Setter
@Entity @Table(name = "budgets")
public class Budget {
  @Id private String id;
  @Enumerated(EnumType.STRING) private BudgetKind kind;
  private String currency;
  private BigDecimal amount;
  private Integer year;
  private Integer month;
  @Column(name = "trip_id") private String tripId;
  private String name;
  @Column(name = "linked_monthly_budget_id") private String linkedMonthlyBudgetId;

  public Budget() {}
  public Budget(String id, BudgetKind kind, String currency, BigDecimal amount) {
    this.id = id; this.kind = kind; this.currency = currency; this.amount = amount;
  }
  // getters/setters…
}
