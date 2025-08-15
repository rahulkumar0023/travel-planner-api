package com.axora.travel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Expense.java
@Getter
@Setter
@Entity @Table(name = "expenses")
public class Expense {
  @Id private String id;

  @Column(name = "trip_id", nullable = false)
  private String tripId;

  private String title;

  // money → BigDecimal + precision/scale to match numeric(12,2)
  @Column(nullable = false, precision = 12, scale = 2)
  private java.math.BigDecimal amount;

  private String category;

  @Column(name = "date")
  private java.time.Instant date; // or OffsetDateTime if you prefer

  @Column(name = "paid_by")
  private String paidBy;

  @Column(name = "created_at")
  private java.time.Instant createdAt = java.time.Instant.now();

  @Column(name = "updated_at")
  private java.time.Instant updatedAt = java.time.Instant.now();

  // getters/setters...
}
