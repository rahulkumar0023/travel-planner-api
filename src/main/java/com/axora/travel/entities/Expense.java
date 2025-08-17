package com.axora.travel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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
  private BigDecimal amount;

  private String category;

  @Column(name = "date")
  private Instant date;

  @Column(name = "paid_by")
  private String paidBy;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  // place with other fields
  @Column(name = "currency", length = 3, nullable = false)
  private String currency;


  // ⬇⬇⬇ NEW: map expense_shared_with(expense_id, participant) as a collection of Strings
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "expense_shared_with",
      joinColumns = @JoinColumn(name = "expense_id"))
  @Column(name = "participant")
  private Set<String> sharedWith = new LinkedHashSet<>();

  @PrePersist
  void prePersist() {
    final var now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }


  public Set<String> getSharedWith() { return sharedWith; }
  public void setSharedWith(Set<String> sharedWith) {
    this.sharedWith = (sharedWith == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(sharedWith);
  }
}
