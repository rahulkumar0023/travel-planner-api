package com.axora.travel.service;

import com.axora.travel.dto.ExpenseCreateRequest;
import com.axora.travel.dto.ExpenseDTO;
import com.axora.travel.entities.Expense;
import com.axora.travel.repository.ExpenseRepository;
import com.axora.travel.repository.TripRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, TripRepository tripRepository) {
        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public ExpenseDTO create(ExpenseCreateRequest req, String createdBy) {
        Expense e = new Expense();
        e.setId(UUID.randomUUID().toString());
        e.setTripId(req.getTripId());
        e.setTitle(req.getTitle());
        e.setAmount(req.getAmount());
        e.setCategory(req.getCategory());
        if (req.getDate() != null) {
            e.setDate(req.getDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        e.setPaidBy(req.getPaidBy());
        e.setSharedWith(req.getSharedWith());
        e.setCreatedBy(createdBy);
        if (req.getCurrency() != null && !req.getCurrency().isBlank()) {
            e.setCurrency(req.getCurrency().toUpperCase());
        } else {
            var trip = tripRepository.findById(req.getTripId()).orElseThrow();
            e.setCurrency(trip.getCurrency());
        }
        Expense saved = expenseRepository.save(e);
        return toDto(saved);
    }

    @Override
    public List<ExpenseDTO> findByTripId(String tripId) {
        return expenseRepository.findByTripIdOrderByDateDescCreatedAtDesc(tripId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> findAll() {
        return expenseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private ExpenseDTO toDto(Expense e) {
        return new ExpenseDTO(
            e.getId(),
            e.getTripId(),
            e.getTitle(),
            e.getAmount(),
            e.getCategory(),
            e.getDate() == null ? null : LocalDateTime.ofInstant(e.getDate(), ZoneOffset.UTC),
            e.getPaidBy(),
            e.getSharedWith() == null ? Set.of() : new HashSet<>(e.getSharedWith())
        );
    }
}
