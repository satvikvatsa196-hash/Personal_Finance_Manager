package com.syfe.finance.model;

public record AppUser(
        long id,
        String username,
        String passwordHash,
        String fullName,
        String phoneNumber
) {
}
