// --- BudgetLinkController start ---
package com.axora.travel.controller;

import com.axora.travel.entities.Budget;
import com.axora.travel.repository.BudgetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budget-links")
@CrossOrigin
public class BudgetLinkController {
    private final BudgetRepository repo;
    public BudgetLinkController(BudgetRepository repo) { this.repo = repo; }

    @DeleteMapping("/{tripBudgetId}")
    public ResponseEntity<Void> deleteLink(@PathVariable String tripBudgetId) {
        Budget b = repo.findById(tripBudgetId).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        b.setLinkedMonthlyBudgetId(null);
        repo.save(b);
        return ResponseEntity.noContent().build();
    }
}
// --- BudgetLinkController end ---