package com.axora.travel.controller;

import com.axora.travel.dto.TripDTO;
import com.axora.travel.entities.Trip;
import com.axora.travel.repository.TripRepository;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
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

  @PutMapping("/{id}")
  public TripDTO updateTripPut(@PathVariable String id, @RequestBody TripDTO dto) {
    var t = trips.findById(id).orElseThrow();
    if (dto.name() != null) t.setName(dto.name());
    if (dto.startDate() != null) t.setStartDate(dto.startDate());
    if (dto.endDate() != null) t.setEndDate(dto.endDate());
    if (dto.currency() != null) t.setCurrency(dto.currency());
    if (dto.initialBudget() != null) t.setInitialBudget(dto.initialBudget());
    if (dto.participants() != null) t.setParticipants(dto.participants());
    t = trips.save(t);
    return toDTO(t);
  }

  @PatchMapping("/{id}")
  public TripDTO updateTripPatch(@PathVariable String id, @RequestBody TripDTO dto) {
    return updateTripPut(id, dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTrip(@PathVariable String id) {
    if (!trips.existsById(id)) return ResponseEntity.notFound().build();
    trips.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}

