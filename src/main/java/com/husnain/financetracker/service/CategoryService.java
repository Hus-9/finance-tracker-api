package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.CategoryRequest;
import com.husnain.financetracker.dto.CategoryResponse;
import com.husnain.financetracker.exception.ConflictException;
import com.husnain.financetracker.exception.ResourceNotFoundException;
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.repository.CategoryRepository;
import com.husnain.financetracker.repository.TransactionRepository;
import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// This is where the logic for categories happens, CategoryController passes everything in here.
// Same as accounts, every method takes the user's id so people only ever touch their own categories.
@Service
@RequiredArgsConstructor

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Gets all of the user's categories, EXPENSE first then INCOME, A to Z.
    public List<CategoryResponse>list(Long userId){

        return categoryRepository.findByUserIdOrderByTypeAscNameAsc(userId).stream()
                .map(CategoryResponse::from)
                .toList();

    }

    // Makes a new category for the user.
    // trim() takes spaces off the start and end so " Food " and "Food" count as the same name.
    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {

        String name = request.name().trim();

        // Stops the user having two categories with the same name and type, it's a 409 if they try.
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(userId, name, request.type())) {

            throw new ConflictException("You already have a " + request.type() + " category called '" + name + "'");
        }

        Category category = Category.builder()
                .user(userRepository.getReferenceById(userId))
                .name(name)
                .type(request.type())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));

    }

    // Rename a category or changes its type.
    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {

        Category category = findOwnedCategory(userId, categoryId);
        String name = request.name().trim();

        // Same duplicate check as create, but it skips this category so keeping the same name isn't a clash.
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndTypeAndIdNot(userId, name, request.type(), categoryId)) {

            throw new ConflictException("You already have a " + request.type() + " category called '" + name + "'");

        }

        // Changing INCOME to EXPENSE (or the other way) isn't allowed once transactions use it.
        // Otherwise the old transactions would end up in a category with the opposite type.
        boolean typeChanged = category.getType() != request.type();
        if (typeChanged && transactionRepository.existsByCategoryId(categoryId)) {

            throw new ConflictException("You can't change the type of a category that already has transactions");

        }

        category.setName(name);
        category.setType(request.type());
        return CategoryResponse.from(categoryRepository.save(category));

    }

    // Deletes a category, but only if no transactions use it.
    // Otherwise those transactions would be left without a category so it's a 409 instead.
    @Transactional
    public void delete(Long userId, Long categoryId) {

        Category category = findOwnedCategory(userId, categoryId);
        if (transactionRepository.existsByCategoryId(categoryId)) {

            throw new ConflictException("This category is used by transactions so it can't be deleted");

        }

        categoryRepository.delete(category);

    }

    // Finds a category that belongs to this user, or throws a 404 if it doesn't exist or isn't theirs.
    private Category findOwnedCategory(Long userId, Long categoryId) {

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    }
}
