package com.axora.travel.service;

import com.axora.travel.dto.BudgetCategoryDTO;
import com.axora.travel.dto.BudgetCategoryRequest;
import com.axora.travel.dto.BudgetSubcategoryDTO;
import com.axora.travel.entities.Budget;
import com.axora.travel.entities.BudgetCategory;
import com.axora.travel.entities.BudgetCategoryType;
import com.axora.travel.entities.BudgetKind;
import com.axora.travel.repository.BudgetCategoryRepository;
import com.axora.travel.repository.BudgetRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BudgetCategoryService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;

    public BudgetCategoryService(BudgetRepository budgetRepository, BudgetCategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetCategoryDTO> list(String ownerEmail, int year, int month) {
        Budget budget = getMonthlyBudget(ownerEmail, year, month);
        return categoryRepository
            .findByMonthlyBudget_IdAndParentIsNullOrderByDisplayOrderAscNameAsc(budget.getId())
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public BudgetCategoryDTO createTopLevel(String ownerEmail, int year, int month, BudgetCategoryRequest request) {
        Budget budget = getMonthlyBudget(ownerEmail, year, month);
        BudgetCategory category = new BudgetCategory();
        category.setId(UUID.randomUUID().toString());
        category.setMonthlyBudget(budget);
        applyCategoryValues(category, request);
        BudgetCategory saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional
    public BudgetSubcategoryDTO createSubcategory(String ownerEmail, String parentId, BudgetCategoryRequest request) {
        BudgetCategory parent = getCategory(parentId, ownerEmail);
        BudgetCategory child = new BudgetCategory();
        child.setId(UUID.randomUUID().toString());
        child.setMonthlyBudget(parent.getMonthlyBudget());
        child.setParent(parent);
        BudgetCategoryType requestedType = request.type();
        if (requestedType != null && requestedType != parent.getType()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subcategory type must match parent type");
        }
        child.setType(parent.getType());
        applyCategoryValues(child, request);
        parent.getChildren().add(child);
        BudgetCategory saved = categoryRepository.save(child);
        return toSubDto(saved);
    }

    @Transactional
    public BudgetCategoryDTO updateCategory(String ownerEmail, String categoryId, BudgetCategoryRequest request) {
        BudgetCategory category = getCategory(categoryId, ownerEmail);
        boolean isTopLevel = category.getParent() == null;
        if (request.type() != null) {
            if (!isTopLevel && request.type() != category.getType()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subcategory type is managed by parent");
            }
            if (isTopLevel) {
                category.setType(request.type());
                category.getChildren().forEach(child -> child.setType(request.type()));
            }
        }
        applyCategoryValues(category, request);
        BudgetCategory saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional
    public void deleteCategory(String ownerEmail, String categoryId) {
        BudgetCategory category = getCategory(categoryId, ownerEmail);
        categoryRepository.delete(category);
    }

    private Budget getMonthlyBudget(String ownerEmail, int year, int month) {
        validateOwner(ownerEmail);
        validateMonth(month);
        return budgetRepository
            .findByKindAndOwnerAndYearAndMonth(BudgetKind.monthly, ownerEmail, year, month)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "monthly budget not found"));
    }

    private BudgetCategory getCategory(String categoryId, String ownerEmail) {
        validateOwner(ownerEmail);
        return categoryRepository
            .findByIdAndMonthlyBudgetOwner(categoryId, ownerEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));
    }

    private void applyCategoryValues(BudgetCategory category, BudgetCategoryRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        if (request.name() != null) {
            String trimmed = request.name().trim();
            if (trimmed.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
            }
            category.setName(trimmed);
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (category.getParent() == null) {
            BudgetCategoryType type = request.type();
            if (type == null && category.getType() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required for categories");
            }
            if (type != null) {
                category.setType(type);
            }
        }
        if (request.plannedAmount() != null) {
            category.setPlannedAmount(normalize(request.plannedAmount()));
        } else if (category.getPlannedAmount() == null) {
            category.setPlannedAmount(BigDecimal.ZERO);
        }
        if (request.actualAmount() != null) {
            category.setActualAmount(normalize(request.actualAmount()));
        } else if (category.getActualAmount() == null) {
            category.setActualAmount(BigDecimal.ZERO);
        }
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }
    }

    private BudgetCategoryDTO toDto(BudgetCategory category) {
        return new BudgetCategoryDTO(
            category.getId(),
            category.getName(),
            category.getType(),
            category.getPlannedAmount(),
            category.getActualAmount(),
            category.getDisplayOrder(),
            category.getChildren().stream()
                .map(this::toSubDto)
                .collect(Collectors.toList())
        );
    }

    private BudgetSubcategoryDTO toSubDto(BudgetCategory category) {
        return new BudgetSubcategoryDTO(
            category.getId(),
            category.getName(),
            category.getType(),
            category.getPlannedAmount(),
            category.getActualAmount(),
            category.getDisplayOrder()
        );
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateOwner(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "owner email is required");
        }
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month must be between 1 and 12");
        }
    }
}
