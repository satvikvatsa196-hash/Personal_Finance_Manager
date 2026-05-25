package com.syfe.finance.service;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.model.SavingsGoal;
import com.syfe.finance.model.Transaction;
import com.syfe.finance.repository.InMemoryStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceServiceTests {
    private static final String USER = "user@example.com";

    @Test
    void categoryNamesAreUniquePerUserAndUsedCategoriesCannotBeDeleted() {
        InMemoryStore store = new InMemoryStore();
        CategoryService categoryService = new CategoryService(store);
        TransactionService transactionService = new TransactionService(store);

        categoryService.create(USER, "Freelance", CategoryType.INCOME);
        assertThrows(ApiException.class, () -> categoryService.create(USER, "Freelance", CategoryType.INCOME));

        transactionService.create(USER, BigDecimal.valueOf(1000), LocalDate.now().minusDays(1), "Freelance", "Project");
        ApiException error = assertThrows(ApiException.class, () -> categoryService.delete(USER, "Freelance"));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void transactionUpdateKeepsOriginalDateAndSupportsFilters() {
        InMemoryStore store = new InMemoryStore();
        TransactionService service = new TransactionService(store);
        LocalDate originalDate = LocalDate.now().minusDays(2);

        Transaction transaction = service.create(USER, BigDecimal.valueOf(25), originalDate, "Food", "Lunch");
        Transaction updated = service.update(USER, transaction.id(), BigDecimal.valueOf(30), null, "Dinner");

        assertEquals(originalDate, updated.date());
        assertEquals("Dinner", updated.description());
        assertEquals(1, service.list(USER, originalDate.minusDays(1), LocalDate.now(), "Food", CategoryType.EXPENSE).size());
    }

    @Test
    void goalsUseNetSavingsSinceStartDate() {
        InMemoryStore store = new InMemoryStore();
        TransactionService transactionService = new TransactionService(store);
        GoalService goalService = new GoalService(store);
        LocalDate start = LocalDate.now().minusDays(10);

        transactionService.create(USER, BigDecimal.valueOf(1200), start.plusDays(1), "Salary", "Income");
        transactionService.create(USER, BigDecimal.valueOf(200), start.plusDays(2), "Food", "Groceries");
        SavingsGoal goal = goalService.create(USER, "Emergency Fund", BigDecimal.valueOf(5000), LocalDate.now().plusMonths(2), start);

        GoalService.GoalProgress progress = goalService.progress(USER, goal);
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(progress.currentProgress()));
        assertEquals(0, BigDecimal.valueOf(20).setScale(2).compareTo(progress.progressPercentage()));
        assertEquals(0, BigDecimal.valueOf(4000).compareTo(progress.remainingAmount()));
    }

    @Test
    void reportsAggregateByCategory() {
        InMemoryStore store = new InMemoryStore();
        TransactionService transactionService = new TransactionService(store);
        ReportService reportService = new ReportService(store);
        LocalDate date = LocalDate.of(2024, 1, 15);

        transactionService.create(USER, BigDecimal.valueOf(3000), date, "Salary", "Pay");
        transactionService.create(USER, BigDecimal.valueOf(400), date, "Food", "Groceries");

        ReportService.ReportData report = reportService.monthly(USER, 2024, 1);
        assertTrue(report.totalIncome().containsKey("Salary"));
        assertTrue(report.totalExpenses().containsKey("Food"));
        assertEquals(0, BigDecimal.valueOf(2600).compareTo(report.netSavings()));
    }
}
