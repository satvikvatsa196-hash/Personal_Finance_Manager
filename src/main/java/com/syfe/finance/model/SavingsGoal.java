package com.syfe.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoal(
        long id,
        String ownerUsername,
        String goalName,
        BigDecimal targetAmount,
        LocalDate targetDate,
        LocalDate startDate
) {
}
