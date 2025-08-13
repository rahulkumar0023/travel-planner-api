package com.rahul.travel;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expenses")
public class Expense {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String tripId;

  private String title;

  @Column(nullable = false)
  private Double amount;

  private String category;
  private LocalDateTime date;

  private String paidBy;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "expense_shared_with", joinColumns = @JoinColumn(name = "expense_id"))
  @Column(name = "participant")
  private Set<String> sharedWith = new HashSet<>();

  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    createdAt = updatedAt = Instant.now();
    if (date == null) {
      date = LocalDateTime.now();
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public Expense() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public Set<String> getSharedWith() {
    return sharedWith;
  }

  public void setSharedWith(Set<String> sharedWith) {
    this.sharedWith = sharedWith;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

