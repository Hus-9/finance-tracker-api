package com.husnain.financetracker.repository;

import com.husnain.financetracker.model.Transaction;
import com.husnain.financetracker.model.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// This builds the WHERE part of the query for listing transactions.
// All the filters are optional so it only adds the ones that were actually sent.
// It's final with a private constructor because it's just a holder for a static method, nobody should make one.
public final class TransactionSpecifications {

    private TransactionSpecifications() {

    }

    // Every parameter except userId can be null, which means "don't filter on this".
    public static Specification<Transaction> filter(Long userId, LocalDate from, LocalDate to, Long categoryId, TransactionType type) {

        // root is the transaction, cb (criteria builder) is what makes each condition.
        return (root, query, cb) -> {

            List<Predicate> conditions = new ArrayList<>();

            // This one is always added, it's what stops anyone seeing someone else's transactions.
            conditions.add(cb.equal(root.get("account").get("user").get("id"), userId));

            // From this date onwards, including the date itself.
            if (from != null) {

                conditions.add(cb.greaterThanOrEqualTo(root.get("date"), from));

            }

            // Up to this date including the date itself.
            if (to != null) {

                conditions.add(cb.lessThanOrEqualTo(root.get("date"), to));

            }

            // Only this category
            if (categoryId != null) {

                conditions.add(cb.equal(root.get("category").get("id"), categoryId));

            }

            // Only INCOME or only EXPENSE.
            if (type != null) {

                conditions.add(cb.equal(root.get("type"), type));

            }

            // Joins them all with AND so a transaction has to match every filter that was sent.
            return cb.and(conditions.toArray(new Predicate[0]));

        };

    }

}
