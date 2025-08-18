package com.axora.travel.service;

import com.axora.travel.dto.ExpenseCreateRequest;
import com.axora.travel.dto.ExpenseDTO;
import java.util.List;

public interface ExpenseService {
    ExpenseDTO create(ExpenseCreateRequest req);
    List<ExpenseDTO> findByTripId(String tripId);
    List<ExpenseDTO> findAll();
}
