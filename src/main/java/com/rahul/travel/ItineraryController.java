package com.rahul.travel;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/itinerary")
@Slf4j
public class ItineraryController {
  public record ItineraryRequest(String destination, int days, String budgetLevel, List<String> interests) {}
  public record DayPlan(int day, List<String> items) {}
  public record ItineraryResponse(String destination, List<DayPlan> plan) {}

  @PostMapping
  public ItineraryResponse generate(@RequestBody ItineraryRequest req) {
    log.info("Generating itinerary for {} over {} days", req.destination(), req.days());
    List<DayPlan> plan = IntStream.rangeClosed(1, Math.max(1, req.days()))
        .mapToObj(d -> new DayPlan(d, List.of(
            "Morning: Coffee & walking tour",
            "Noon: Local restaurant",
            "Afternoon: Museum",
            "Evening: Food market")))
        .toList();
    return new ItineraryResponse(req.destination(), plan);
  }
}

