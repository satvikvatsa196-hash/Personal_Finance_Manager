package com.syfe.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        long id,
        String ownerUsername,
        BigDecimal amount,
        LocalDate date,
        String category,
        String description,
        CategoryType type
) {
}
