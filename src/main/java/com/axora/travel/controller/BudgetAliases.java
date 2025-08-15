package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/budget", "/api/budget"})
class BudgetAliases {
    private final BudgetController real;
    BudgetAliases(BudgetController real) { this.real = real; }

    @PostMapping({"", "/", "/monthly", "/trip"})
    public ResponseEntity<Budget> create(@RequestBody BudgetController.CreateReq req) {
        return real.create(req);
    }
}
