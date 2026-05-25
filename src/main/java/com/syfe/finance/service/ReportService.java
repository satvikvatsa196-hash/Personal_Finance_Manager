package com.syfe.finance.service;

import com.syfe.finance.model.CategoryType;
import com.syfe.finance.repository.InMemoryStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReportService {
    private final InMemoryStore store;

    public ReportService(InMemoryStore store) {
        this.store = store;
    }

    public ReportData monthly(String username, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return aggregate(username, start, end);
    }

    public ReportData yearly(String username, int year) {
        return aggregate(username, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    private ReportData aggregate(String username, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> income = new TreeMap<>();
        Map<String, BigDecimal> expenses = new TreeMap<>();
        store.transactionsFor(username).stream()
                .filter(transaction -> !transaction.date().isBefore(start) && !transaction.date().isAfter(end))
                .forEach(transaction -> {
                    Map<String, BigDecimal> target = transaction.type() == CategoryType.INCOME ? income : expenses;
                    target.merge(transaction.category(), transaction.amount(), BigDecimal::add);
                });
        BigDecimal totalIncome = income.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReportData(income, expenses, totalIncome.subtract(totalExpenses));
    }

    public record ReportData(Map<String, BigDecimal> totalIncome, Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
    }
}
