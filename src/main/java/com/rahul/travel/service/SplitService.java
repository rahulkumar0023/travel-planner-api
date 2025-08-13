package com.rahul.travel.service;

import com.rahul.travel.Expense;
import com.rahul.travel.Trip;
import com.rahul.travel.dto.GroupBalanceDTO;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SplitService {
  public List<GroupBalanceDTO> computeBalances(Trip trip, List<Expense> expenses) {
    Map<String, Double> net = trip.getParticipants().stream()
        .collect(Collectors.toMap(p -> p, p -> 0.0));

    for (Expense e : expenses) {
      if (e.getSharedWith() == null || e.getSharedWith().isEmpty()) continue;
      double share = e.getAmount() / e.getSharedWith().size();
      net.merge(e.getPaidBy(), e.getAmount(), Double::sum);
      for (String member : e.getSharedWith()) {
        net.merge(member, -share, Double::sum);
      }
    }

    record Node(String name, double amt) {}
    PriorityQueue<Node> debtors = new PriorityQueue<>(Comparator.comparingDouble(n -> n.amt));
    PriorityQueue<Node> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.amt, a.amt));

    net.forEach((name, amt) -> {
      double r = round2(amt);
      if (r < 0) debtors.add(new Node(name, r));
      if (r > 0) creditors.add(new Node(name, r));
    });

    List<GroupBalanceDTO> settlements = new ArrayList<>();
    while (!debtors.isEmpty() && !creditors.isEmpty()) {
      Node d = debtors.poll();
      Node c = creditors.poll();
      double pay = Math.min(-d.amt, c.amt);
      pay = round2(pay);
      if (pay > 0) {
        settlements.add(new GroupBalanceDTO(d.name(), c.name(), pay, trip.getCurrency()));
      }
      double dRem = round2(d.amt + pay);
      double cRem = round2(c.amt - pay);
      if (dRem < 0) debtors.add(new Node(d.name(), dRem));
      if (cRem > 0) creditors.add(new Node(c.name(), cRem));
    }
    return settlements;
  }

  private double round2(double v) {
    return Math.round(v * 100.0) / 100.0;
  }
}

