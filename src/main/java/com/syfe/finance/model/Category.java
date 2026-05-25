package com.syfe.finance.model;

public record Category(
        String name,
        CategoryType type,
        boolean custom,
        String ownerUsername
) {
}
