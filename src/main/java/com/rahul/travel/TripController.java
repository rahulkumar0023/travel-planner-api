package com.rahul.travel;

import com.rahul.travel.dto.TripDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trips")
@Validated
public class TripController {
  private final TripRepository trips;

  public TripController(TripRepository trips) {
    this.trips = trips;
  }

  @PostMapping
  public TripDTO create(@RequestBody @Valid TripDTO dto) {
    Trip t = new Trip();
    t.setName(dto.name());
    t.setStartDate(dto.startDate());
    t.setEndDate(dto.endDate());
    t.setCurrency(dto.currency());
    t.setInitialBudget(dto.initialBudget());
    t.setParticipants(dto.participants() == null ? Set.of() : dto.participants());
    t = trips.save(t);
    return toDTO(t);
  }

  @GetMapping
  public List<TripDTO> all() {
    return trips.findAll().stream().map(this::toDTO).toList();
  }

  private TripDTO toDTO(Trip t) {
    return new TripDTO(t.getId(), t.getName(), t.getStartDate(), t.getEndDate(),
        t.getInitialBudget(), t.getCurrency(), t.getParticipants());
  }
}

