package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// This is how we talk to the budgets table.
// The two list queries are written by hand so they can load the category in  the same query with join fetch.

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Gets all of a user's budgets, newest month first and A to Z by category name inside each month.

    @Query("""
            select b from Budget b 
            join fetch b.category c 
            where b.user.id = :userId
            order by b.month desc, c.name asc
            """)
    List<Budget> findAllByUserId(@Param("userId") Long userId);

    // Gets a user's budgets for just one month, A to Z by category name.
    @Query("""
            select b from Budget b 
            join fetch b.category c 
            where b.user.id = :userId and b.month = :month
            order by c.name asc 
            """)
    List<Budget> findAllByUserIdAndMonth(@Param("userId") Long userId, @Param("month") String month);

    // Checks if the user already has a budget for this category in this month, used to stop duplicates.
    boolean existsByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, String month);

    // Checks if any budget uses this category, CategoryService uses it before changing a category's type.
    boolean existsByCategoryId(Long categoryId);

}
