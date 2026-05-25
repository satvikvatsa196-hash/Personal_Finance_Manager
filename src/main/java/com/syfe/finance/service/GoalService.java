package com.syfe.finance.service;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.model.SavingsGoal;
import com.syfe.finance.model.Transaction;
import com.syfe.finance.repository.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {
    private final InMemoryStore store;

    public GoalService(InMemoryStore store) {
        this.store = store;
    }

    public SavingsGoal create(String username, String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate) {
        LocalDate effectiveStartDate = startDate == null ? LocalDate.now() : startDate;
        validateTarget(targetAmount, targetDate, effectiveStartDate);
        if (goalName == null || goalName.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Goal name is required");
        }
        return store.saveGoal(new SavingsGoal(0, username, goalName.trim(), targetAmount, targetDate, effectiveStartDate));
    }

    public List<SavingsGoal> list(String username) {
        return store.goalsFor(username);
    }

    public SavingsGoal get(String username, long id) {
        return requireOwned(username, id);
    }

    public SavingsGoal update(String username, long id, BigDecimal targetAmount, LocalDate targetDate) {
        SavingsGoal existing = requireOwned(username, id);
        BigDecimal newAmount = targetAmount == null ? existing.targetAmount() : targetAmount;
        LocalDate newDate = targetDate == null ? existing.targetDate() : targetDate;
        validateTarget(newAmount, newDate, existing.startDate());
        return store.saveGoal(new SavingsGoal(existing.id(), username, existing.goalName(), newAmount, newDate, existing.startDate()));
    }

    public void delete(String username, long id) {
        requireOwned(username, id);
        store.deleteGoal(id);
    }

    public GoalProgress progress(String username, SavingsGoal goal) {
        BigDecimal progress = store.transactionsFor(username).stream()
                .filter(transaction -> !transaction.date().isBefore(goal.startDate()))
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percentage = progress.multiply(BigDecimal.valueOf(100))
                .divide(goal.targetAmount(), 2, RoundingMode.HALF_UP);
        BigDecimal remaining = goal.targetAmount().subtract(progress).max(BigDecimal.ZERO);
        return new GoalProgress(progress, percentage, remaining);
    }

    private SavingsGoal requireOwned(String username, long id) {
        SavingsGoal goal = store.findGoal(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Goal not found"));
        if (!username.equals(goal.ownerUsername())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Goal belongs to another user");
        }
        return goal;
    }

    private BigDecimal signedAmount(Transaction transaction) {
        return transaction.type() == CategoryType.INCOME ? transaction.amount() : transaction.amount().negate();
    }

    private void validateTarget(BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target amount must be positive");
        }
        if (targetDate == null || !targetDate.isAfter(startDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target date must be after start date");
        }
    }

    public record GoalProgress(BigDecimal currentProgress, BigDecimal progressPercentage, BigDecimal remainingAmount) {
    }
}
