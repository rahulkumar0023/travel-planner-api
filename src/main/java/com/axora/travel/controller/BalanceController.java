package com.axora.travel.controller;

import com.axora.travel.dto.TransferDTO;
import com.axora.travel.repository.TripRepository;
import com.axora.travel.security.AppPrincipal;
import com.axora.travel.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/balances")
@CrossOrigin(origins = "*")
@Slf4j
public class BalanceController {
  private final BalanceService service;
  private final TripRepository trips;

  public BalanceController(BalanceService service, TripRepository trips) {
    this.service = service; this.trips = trips;
  }

  private void assertMember(String tripId, String email) {
    var t = trips.findById(tripId).orElseThrow();
    boolean owner = email != null && email.equals(t.getOwner());
    boolean participant = t.getParticipants() != null && t.getParticipants().contains(email);
    if (!(owner || participant)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of trip");
  }

  @GetMapping("/{tripId}")
  public List<TransferDTO> getBalances(@PathVariable String tripId,
                                       @AuthenticationPrincipal AppPrincipal me) {
    log.info("Received request to compute balances for trip {}", tripId);
    assertMember(tripId, me.email());
    return service.compute(tripId);
  }
}

