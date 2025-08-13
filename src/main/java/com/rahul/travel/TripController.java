package com.rahul.travel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trips")
@CrossOrigin(origins = "*")
public class TripController {
  @Autowired private TripRepository repo;

  @PostMapping
  public Trip save(@RequestBody Trip trip) {
    return repo.save(trip);
  }

  @GetMapping
  public List<Trip> all() {
    return repo.findAll();
  }
}
