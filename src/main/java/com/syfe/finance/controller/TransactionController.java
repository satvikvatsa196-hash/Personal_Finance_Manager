package com.syfe.finance.controller;

import com.syfe.finance.model.CategoryType;
import com.syfe.finance.model.Transaction;
import com.syfe.finance.service.CategoryService;
import com.syfe.finance.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(Authentication authentication, @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.create(authentication.getName(), request.amount(), request.date(), request.category(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction));
    }

    @GetMapping
    public Map<String, List<TransactionResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) CategoryType type) {
        String categoryFilter = category != null ? category : resolveCategoryName(authentication.getName(), categoryId);
        return Map.of("transactions", transactionService.list(authentication.getName(), startDate, endDate, categoryFilter, type).stream()
                .map(TransactionResponse::from)
                .toList());
    }

    @PutMapping("/{id}")
    public TransactionResponse update(Authentication authentication, @PathVariable long id, @Valid @RequestBody UpdateTransactionRequest request) {
        return TransactionResponse.from(transactionService.update(authentication.getName(), id, request.amount(), request.category(), request.description()));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(Authentication authentication, @PathVariable long id) {
        transactionService.delete(authentication.getName(), id);
        return Map.of("message", "Transaction deleted successfully");
    }

    public record CreateTransactionRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotNull LocalDate date,
            @NotBlank String category,
            String description
    ) {
    }

    public record UpdateTransactionRequest(
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            String category,
            String description
    ) {
    }

    public record TransactionResponse(long id, BigDecimal amount, LocalDate date, String category, String description, CategoryType type) {
        static TransactionResponse from(Transaction transaction) {
            return new TransactionResponse(transaction.id(), transaction.amount(), transaction.date(), transaction.category(), transaction.description(), transaction.type());
        }
    }

    private String resolveCategoryName(String username, Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        List<String> names = categoryService.list(username).stream().map(category -> category.name()).toList();
        return categoryId > 0 && categoryId <= names.size() ? names.get(categoryId - 1) : null;
    }
}
