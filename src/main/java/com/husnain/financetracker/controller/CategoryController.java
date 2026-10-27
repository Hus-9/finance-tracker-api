package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.CategoryRequest;
import com.husnain.financetracker.dto.CategoryResponse;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller handles the category endpoints, it passes everything to CategoryService.
// Every endpoint in here starts with /api/categories.
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor

public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories gives back all the logged in user's categories.
    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal UserPrincipal user) {

        return categoryService.list(user.getId());

    }

    // POST /api/categories makes a new category, 201 Created if it works or 409 if they already have one with that name.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody CategoryRequest request) {

        return categoryService.create(user.getId(), request);

    }

    // PUT /api/categories/{id} renames a category or changes its type.
    @PutMapping("/{id}")
    public CategoryResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {

        return categoryService.update(user.getId(), id, request);

    }

    // DELETE /api/categories/{id} deletes a category, 204 if it works or 409 if transactions still use it.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {

        categoryService.delete(user.getId(), id);

    }
}
