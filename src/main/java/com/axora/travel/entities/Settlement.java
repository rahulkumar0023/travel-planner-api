package com.axora.travel.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Setter
@Entity @Table(name = "settlements")
public class Settlement {
    @Id private String id;
    @Column(name="trip_id") private String tripId;
    private String payer;
    private String payee;
    private String currency;
    private BigDecimal amount;
    private String note;
    @Column(name="created_at") private Instant createdAt = Instant.now();
    // getters/setters…
}