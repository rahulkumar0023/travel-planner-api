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

  // --- getters/setters ---

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getTripId() { return tripId; }
  public void setTripId(String tripId) { this.tripId = tripId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }

  public Instant getDate() { return date; }
  public void setDate(Instant date) { this.date = date; }

  public String getPaidBy() { return paidBy; }
  public void setPaidBy(String paidBy) { this.paidBy = paidBy; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

  // ⬇⬇⬇ these resolve your compile errors
  public Set<String> getSharedWith() { return sharedWith; }
  public void setSharedWith(Set<String> sharedWith) {
    this.sharedWith = (sharedWith == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(sharedWith);
  }
}
