package com.syfe.finance.service;

import com.syfe.finance.exception.ApiException;
import com.syfe.finance.model.Category;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.repository.InMemoryStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final InMemoryStore store;

    public CategoryService(InMemoryStore store) {
        this.store = store;
    }

    public List<Category> list(String username) {
        return store.categoriesFor(username);
    }

    public Category create(String username, String name, CategoryType type) {
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        if (store.categoryNameExists(username, name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Category name already exists");
        }
        return store.saveCategory(username, name.trim(), type);
    }

    public void delete(String username, String name) {
        Category category = store.findCategory(username, name)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        if (!category.custom()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Default categories cannot be deleted");
        }
        if (store.categoryInUse(username, category.name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category is used by transactions");
        }
        store.deleteCustomCategory(username, category.name());
    }
}
