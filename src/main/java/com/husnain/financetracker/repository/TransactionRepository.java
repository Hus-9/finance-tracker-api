package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.Transaction;
import com.husnain.financetracker.dto.CategorySpending;
import com.husnain.financetracker.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

// This is how we talk to the transactions table.
// JpaSpecificationExecutor lets us build a query bit by bit, which we need for the optional filters on the list endpoint.
// A transaction doesn't have a user column, it belongs to a user through its account so the queries check account.user.id.

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // Gets one transaction but only if its account belongs to this user.
    // This one's written by hand with @Query because the name would be too long and messy.
    // join fetch loads the account and category in the same query so we don't need extra queries to build response.
    @Query("""
            select t from Transaction t 
            join fetch t.account a 
            join fetch t.category c 
            where t.id = :id and a.user.id = :userId
            """)
    Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // The list endpoint uses this with the filters from TransactionSpecifications and a page size.
    // @EntityGraph makes it load the account and category for every transaction in one go instead of one query each
    @Override
    @EntityGraph(attributePaths = {"account", "category"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    // Checks if any transaction uses this category, used before deleting a category.
    boolean existsByCategoryId(Long categoryId);

    // Adds up all of one type of transaction for a user between two dates, like all the EXPENSE ones in October.
    // sum gives back null if there's nothing to add up, ReportService turns that into 0.00.
    @Query("""
            select sum(t.amount) from Transaction t 
            where  t.account.user.id = :userId
                and t.type = :type
                and t.date >= :from and t.date <= :to
            """)
    BigDecimal sumAmount(@Param("userId") Long userId,
                         @Param("type") TransactionType type,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);

    // Same idea but it gives one total per category, biggest first.
    // select new builds a CategorySpending straight from each row of the result.
    // group by puts all the transaction with the same category together so sum adds each category up on its own.
    @Query("""
            select new com.husnain.financetracker.dto.CategorySpending(c.id, c.name, sum(t.amount))
            from Transaction t
            join t.category c
            where t.account.user.id = :userId
                and t.type = :type
                and t.date >= :from and t.date <= :to
            group by c.id, c.name
            order by sum(t.amount) desc, c.name asc
            """)
    List<CategorySpending> sumAmountByCategory(@Param("userId") Long userId,
                                               @Param("type") TransactionType type,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

}
