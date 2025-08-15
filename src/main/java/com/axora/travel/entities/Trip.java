package com.axora.travel.entities;

 import jakarta.persistence.*;
 import lombok.Getter;
 import lombok.Setter;
import java.math.BigDecimal;
 import java.time.Instant;
 import java.time.LocalDate;
 import java.util.HashSet;
 import java.util.Set;

@Entity
@Table(name = "trips")
@Getter
@Setter
public class Trip {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

   private String name;
   private LocalDate startDate;
   private LocalDate endDate;

  @Column(length = 3)                 // ISO code like "EUR"
  private String currency;
  @Column(name = "initial_budget", precision = 12, scale = 2)
  private BigDecimal initialBudget;

   @ElementCollection(fetch = FetchType.EAGER)
   @CollectionTable(name = "trip_participants", joinColumns = @JoinColumn(name = "trip_id"))
   @Column(name = "participant")
   private Set<String> participants = new HashSet<>();

   private Instant createdAt;
   private Instant updatedAt;

   public String getCurrency() {
     return currency;
   }

   public void setCurrency(String currency) {
     this.currency = currency;
   }

  public BigDecimal getInitialBudget() {
    return initialBudget;
  }
  public void setInitialBudget(BigDecimal initialBudget) {
    this.initialBudget = initialBudget;
  }
}

