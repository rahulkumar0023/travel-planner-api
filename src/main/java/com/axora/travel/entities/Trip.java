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
    @Column(name = "spend_currencies")
    private String spendCurrencies; // CSV string: "TRY,INR"

  @Column(name = "initial_budget", precision = 12, scale = 2)
  private BigDecimal initialBudget;

   @ElementCollection(fetch = FetchType.EAGER)
   @CollectionTable(name = "trip_participants", joinColumns = @JoinColumn(name = "trip_id"))
   @Column(name = "participant")
   private Set<String> participants = new HashSet<>();

    // Map to snake_case columns and require values
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }


  public BigDecimal getInitialBudget() {
    return initialBudget;
  }
  public void setInitialBudget(BigDecimal initialBudget) {
    this.initialBudget = initialBudget;
  }
}

