package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/budget", "/api/budget"})
class BudgetAliases {
    private final BudgetController real;
    BudgetAliases(BudgetController real) { this.real = real; }

    @PostMapping({"", "/", "/monthly", "/trip"})
    public ResponseEntity<Budget> create(@RequestBody BudgetController.CreateReq req) {
        return real.create(req);
    }

    // --- BudgetAliases extra routes start ---
    @PutMapping("/{id}")
    @PatchMapping("/{id}")
    public ResponseEntity<Budget> updateAlias(@PathVariable String id,
                                              @RequestBody BudgetController.CreateReq req) {
        return real.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlias(@PathVariable String id) {
        return real.delete(id);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteAliasPost(@PathVariable String id) {
        return real.delete(id);
    }

    @PostMapping("/{id}/unlink")
    public ResponseEntity<Budget> unlinkAlias(@PathVariable String id) {
        return real.unlink(id);
    }
// --- BudgetAliases extra routes end ---
}
