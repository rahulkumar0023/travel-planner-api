package com.axora.travel.controller;

import com.axora.travel.dto.BudgetCategoryDTO;
import com.axora.travel.dto.BudgetCategoryRequest;
import com.axora.travel.dto.BudgetSubcategoryDTO;
import com.axora.travel.security.AppPrincipal;
import com.axora.travel.service.BudgetCategoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping({"/budget-categories", "/api/budget-categories"})
public class BudgetCategoryController {

    private final BudgetCategoryService service;

    public BudgetCategoryController(BudgetCategoryService service) {
        this.service = service;
    }

    @GetMapping("/monthly/{year}/{month}")
    public List<BudgetCategoryDTO> listCategories(@PathVariable int year,
                                                  @PathVariable int month,
                                                  @AuthenticationPrincipal AppPrincipal principal) {
        String owner = requirePrincipal(principal);
        return service.list(owner, year, month);
    }

    @PostMapping("/monthly/{year}/{month}")
    public ResponseEntity<BudgetCategoryDTO> createCategory(@PathVariable int year,
                                                            @PathVariable int month,
                                                            @RequestBody BudgetCategoryRequest request,
                                                            @AuthenticationPrincipal AppPrincipal principal) {
        String owner = requirePrincipal(principal);
        BudgetCategoryDTO created = service.createTopLevel(owner, year, month, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{parentId}/children")
    public ResponseEntity<BudgetSubcategoryDTO> createSubcategory(@PathVariable String parentId,
                                                                  @RequestBody BudgetCategoryRequest request,
                                                                  @AuthenticationPrincipal AppPrincipal principal) {
        String owner = requirePrincipal(principal);
        BudgetSubcategoryDTO created = service.createSubcategory(owner, parentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{categoryId}")
    public BudgetCategoryDTO updateCategory(@PathVariable String categoryId,
                                            @RequestBody BudgetCategoryRequest request,
                                            @AuthenticationPrincipal AppPrincipal principal) {
        String owner = requirePrincipal(principal);
        return service.updateCategory(owner, categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId,
                                               @AuthenticationPrincipal AppPrincipal principal) {
        String owner = requirePrincipal(principal);
        service.deleteCategory(owner, categoryId);
        return ResponseEntity.noContent().build();
    }

    private String requirePrincipal(AppPrincipal principal) {
        if (principal == null || principal.email() == null || principal.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthenticated");
        }
        return principal.email();
    }
}
