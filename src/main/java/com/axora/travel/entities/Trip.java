package com.axora.travel.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trips")
public class Trip {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String currency;
    private Double initialBudget;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "trip_participants", joinColumns = @JoinColumn(name = "trip_id"))
  @Column(name = "participant")
  private Set<String> participants = new HashSet<>();

  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public Trip() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Double getInitialBudget() {
    return initialBudget;
  }

  public void setInitialBudget(Double initialBudget) {
    this.initialBudget = initialBudget;
  }

  public Set<String> getParticipants() {
    return participants;
  }

  public void setParticipants(Set<String> participants) {
    this.participants = participants;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}

