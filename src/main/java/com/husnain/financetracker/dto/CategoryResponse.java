package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.TransactionType;

// This is what the API sends back for a category, the user it belongs to stay hidden.
public record CategoryResponse(Long id, String name, TransactionType type) {

    // Turns a Category from the database into a CategoryResponse.
    public static CategoryResponse from(Category category) {

        return new CategoryResponse(category.getId(), category.getName(), category.getType());

    }
}
