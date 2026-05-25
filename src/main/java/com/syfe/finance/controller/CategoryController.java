package com.syfe.finance.controller;

import com.syfe.finance.model.Category;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.service.CategoryService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Map<String, List<CategoryResponse>> list(Authentication authentication) {
        return Map.of("categories", categoryService.list(authentication.getName()).stream().map(CategoryResponse::from).toList());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(Authentication authentication, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.create(authentication.getName(), request.name(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(category));
    }

    @DeleteMapping("/{name}")
    public Map<String, String> delete(Authentication authentication, @PathVariable String name) {
        categoryService.delete(authentication.getName(), name);
        return Map.of("message", "Category deleted successfully");
    }

    public record CategoryRequest(@NotBlank String name, @NotNull CategoryType type) {
    }

    public record CategoryResponse(String name, CategoryType type, @JsonProperty("isCustom") boolean isCustom, boolean custom) {
        static CategoryResponse from(Category category) {
            return new CategoryResponse(category.name(), category.type(), category.custom(), category.custom());
        }
    }
}
