package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.PageResponse;
import com.husnain.financetracker.dto.TransactionRequest;
import com.husnain.financetracker.dto.TransactionResponse;
import com.husnain.financetracker.exception.BadRequestException;
import com.husnain.financetracker.exception.ResourceNotFoundException;
import com.husnain.financetracker.model.Account;
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.Transaction;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.repository.AccountRepository;
import com.husnain.financetracker.repository.CategoryRepository;
import com.husnain.financetracker.repository.TransactionRepository;
import com.husnain.financetracker.repository.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// This is where the logic for transactions happens, TransactionController passes everything in here.
// Every method takes the user's id and every account and category gets checked to make sure it's theirs.
@Service
@RequiredArgsConstructor

public class TransactionService {

    // The most transactions one page can have so nobody can ask for a million rows at once.
    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    // Lists the user's transactions with the optional filters, one page at a time.
    public PageResponse<TransactionResponse> list(Long userId, LocalDate from, LocalDate to, Long categoryId, TransactionType type, int page, int size) {

        // Page starts at 0 and the size has to be between 1 and 100.
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {

            throw new BadRequestException("page must be 0 or more and size must be between 1 and " + MAX_PAGE_SIZE);

        }

        // A date range where the start is after the end can never match anything so it's probably a mistake.
        if (from != null && to != null && from.isAfter(to)) {

            throw new BadRequestException("'from' must not be after 'to'");

        }

        // Newest first and if two have the same date the newest id comes first so the order never jumps around between pages.
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id")));

        // Gets the page from the database with the filters, turns each transaction into a response, then wraps it in our PageResponse.
        return PageResponse.from(transactionRepository.findAll(TransactionSpecifications.filter(userId, from, to, categoryId, type), pageable)
                .map(TransactionResponse::from));

    }

    // Gets one transaction, it's a 404 if it doesn't exist or isn't theirs.
    public TransactionResponse get(Long userId, Long transactionId) {

        return TransactionResponse.from(findOwnedTransaction(userId, transactionId));

    }

    // Makes a new transaction.

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {

        // Both the account and the category have to belong to this user, otherwise it's a 404.
        // This stops someone adding a transaction to another person's account by guessing its id.
        Account account = findOwnedAccount(userId, request.accountId());
        Category category = findOwnedCategory(userId, request.categoryId());
        checkTypeMatches(request.type(), category);

        Transaction transaction = Transaction.builder()
                .account(account)
                .category(category)
                .amount(toMoney(request.amount()))
                .type(request.type())
                .description(emptyToNull(request.description()))
                .date(request.date())
                .build();
        return TransactionResponse.from(transactionRepository.save(transaction));

    }

    // Updates a transaction with new details, same checks as create.
    @Transactional
    public TransactionResponse update(Long userId, Long transactionId, TransactionRequest request) {

        Transaction transaction = findOwnedTransaction(userId, transactionId);
        Account account = findOwnedAccount(userId, request.accountId());
        Category category = findOwnedCategory(userId, request.categoryId());
        checkTypeMatches(request.type(), category);

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(toMoney(request.amount()));
        transaction.setType(request.type());
        transaction.setDescription(emptyToNull(request.description()));
        transaction.setDate(request.date());
        return TransactionResponse.from(transactionRepository.save(transaction));

    }

    // Deletes a transaction, it checks it's theirs first
    @Transactional
    public void delete(Long userId, Long transactionId) {

        transactionRepository.delete(findOwnedTransaction(userId, transactionId));

    }

    private Transaction findOwnedTransaction(Long userId, Long transactionId) {

        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    }

    private Account findOwnedAccount(Long userId, Long accountId) {

        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    }

    private Category findOwnedCategory(Long userId, Long categoryId) {

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    }

    // An INCOME transaction has to go in an INCOME category and an EXPENSE one in an EXPENSE category.
    // Otherwise the reports later would add things up wrong so it's a 400 if they don't match.

    private void checkTypeMatches(TransactionType type, Category category) {

        if (category.getType() != type) {

            throw new BadRequestException("A " + type + " transaction can't go in the " + category.getType() + " category '" + category.getName() + "'");

        }

    }

    // Makes every amount have exactly 2 decimal places so 12.5 becomes 12.50.
    // UNNECESSARY means it should never need to round which is true because @Digits already blocked anything with more than 2 decimals.
    private BigDecimal toMoney(BigDecimal amount) {

        return amount.setScale(2, RoundingMode.UNNECESSARY);

    }

    // Turns an empty or all-spaces description into null so the database doesn't fill up with blank notes.
    private String emptyToNull(String text) {

        if (text == null || text.isBlank()) {

            return null;

        }

        return text.trim();

    }
}
