package com.axora.travel.controller;

import com.axora.travel.dto.TripDTO;
import com.axora.travel.entities.Trip;
import com.axora.travel.repository.TripRepository;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/trips")
@Validated
@Slf4j
public class TripController {
  private final TripRepository trips;

  public TripController(TripRepository trips) {
    this.trips = trips;
  }

  @PostMapping
  public TripDTO create(@RequestBody @Valid TripDTO dto) {
    log.info("Received request to create trip {}", dto.name());
    Trip t = new Trip();
    t.setName(dto.name());
    t.setStartDate(dto.startDate());
    t.setEndDate(dto.endDate());
    t.setCurrency(dto.currency());
    t.setInitialBudget(dto.initialBudget());           // ← DTO is BigDecimal no
    t.setParticipants(dto.participants() == null ? Set.of() : dto.participants());
    t = trips.save(t);
    return toDTO(t);
  }

  @GetMapping
  public List<TripDTO> all() {
    log.info("Received request to list all trips");
    return trips.findAll().stream().map(this::toDTO).toList();
  }

  private TripDTO toDTO(Trip t) {
       return new TripDTO(                               // ← match DTO field order
                   t.getId(),
                   t.getName(),
                   t.getStartDate(),
                   t.getEndDate(),
                   t.getCurrency(),
                   t.getInitialBudget(),
                   t.getParticipants());
  }
}

