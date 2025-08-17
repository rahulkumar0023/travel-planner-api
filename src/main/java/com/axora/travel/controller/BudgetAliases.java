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

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateAliasPut(@PathVariable String id,
                                                 @RequestBody BudgetController.CreateReq req) { return real.updatePut(id, req); }

    @PatchMapping("/{id}")
    public ResponseEntity<Budget> updateAliasPatch(@PathVariable String id,
                                                   @RequestBody BudgetController.CreateReq req) { return real.updatePut(id, req); }

    @PostMapping("/{id}")
    public ResponseEntity<Budget> updateAliasPost(@PathVariable String id,
                                                  @RequestBody BudgetController.CreateReq req) { return real.updatePut(id, req); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlias(@PathVariable String id) { return real.delete(id); }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteAliasPost(@PathVariable String id) { return real.delete(id); }

    @PostMapping("/{id}/unlink")
    public ResponseEntity<Budget> unlinkAlias(@PathVariable String id) { return real.unlink(id); }
}
