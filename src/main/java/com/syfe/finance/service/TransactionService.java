package com.syfe.finance.service;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.Category;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.model.Transaction;
import com.syfe.finance.repository.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    private final InMemoryStore store;

    public TransactionService(InMemoryStore store) {
        this.store = store;
    }

    public Transaction create(String username, BigDecimal amount, LocalDate date, String categoryName, String description) {
        validateAmount(amount);
        validatePastOrPresent(date);
        Category category = store.findCategory(username, categoryName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid category"));
        return store.saveTransaction(new Transaction(0, username, amount, date, category.name(), description, category.type()));
    }

    public List<Transaction> list(String username, LocalDate startDate, LocalDate endDate, String category, CategoryType type) {
        return store.transactionsFor(username).stream()
                .filter(transaction -> startDate == null || !transaction.date().isBefore(startDate))
                .filter(transaction -> endDate == null || !transaction.date().isAfter(endDate))
                .filter(transaction -> category == null || transaction.category().equals(category))
                .filter(transaction -> type == null || transaction.type() == type)
                .toList();
    }

    public Transaction update(String username, long id, BigDecimal amount, String categoryName, String description) {
        Transaction existing = requireOwned(username, id);
        BigDecimal newAmount = amount == null ? existing.amount() : amount;
        validateAmount(newAmount);
        Category category = categoryName == null ? null : store.findCategory(username, categoryName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid category"));
        return store.saveTransaction(new Transaction(
                existing.id(),
                username,
                newAmount,
                existing.date(),
                category == null ? existing.category() : category.name(),
                description == null ? existing.description() : description,
                category == null ? existing.type() : category.type()
        ));
    }

    public void delete(String username, long id) {
        requireOwned(username, id);
        store.deleteTransaction(id);
    }

    public Transaction requireOwned(String username, long id) {
        Transaction transaction = store.findTransaction(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transaction not found"));
        if (!username.equals(transaction.ownerUsername())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Transaction belongs to another user");
        }
        return transaction;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
    }

    private void validatePastOrPresent(LocalDate date) {
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date is required");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date cannot be in the future");
        }
    }
}
