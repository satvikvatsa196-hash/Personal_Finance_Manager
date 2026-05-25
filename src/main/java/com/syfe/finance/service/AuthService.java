package com.syfe.finance.service;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.AppUser;
import com.syfe.finance.repository.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final InMemoryStore store;
    private final PasswordEncoder passwordEncoder;

    public AuthService(InMemoryStore store, PasswordEncoder passwordEncoder) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String username, String password, String fullName, String phoneNumber) {
        if (store.findUser(username).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        AppUser user = store.createUser(username, passwordEncoder.encode(password), fullName, phoneNumber);
        if (user == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        return user;
    }
}
