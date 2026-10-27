package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// This is how we talk to the categories table.
// Same as the other repositories, Spring builds the queries from the method names.
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Gets all of a user's categories, EXPENSE ones first then INCOME and A to Z inside each.
    List<Category> findByUserIdOrderByTypeAscNameAsc(Long userId);

    // Gets one category but only if it belongs to this user, same idea as findByIdAndUserId for accounts.
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // Checks if the user already has a category with this name and type.
    // IgnoreCase means Food and food count as the same which matches the unique index in the database.
    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);

    // Same check but it skips one category, used when renaming so a category doesn't clash with itself.
    boolean existsByUserIdAndNameIgnoreCaseAndTypeAndIdNot(Long userId, String name, TransactionType type, Long id);

}
