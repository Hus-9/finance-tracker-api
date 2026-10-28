package com.husnain.financetracker.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Units tests for TransactionService so there's no database and no Spring, just this one class on its own.
// @Mock makes fake repositories that do nothing unless we tell them what to give back.
// @InjectMocks makes a real TransactionService and passes the fake  repositories into it.
// Each test follows the same pattern, set things up, run the method, then check what happened.
@ExtendWith(MockitoExtension.class)

class TransactionServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 10, 14);

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    // Some made up data the tests share, Food is EXPENSE and Salary is INCOME.
    private final Account account = Account.builder().id(2L).name("Monzo").build();
    private final Category food = Category.builder().id(3L).name("Food").type(TransactionType.EXPENSE).build();
    private final Category salary = Category.builder().id(4L).name("Salary").type(TransactionType.INCOME).build();

    @Test
    void createSavesTheAmountWithTwoDecimalPlaces() {

        // The amount and category both belong to the user and save just gives back whatever it was given.
        when(accountRepository.findByIdAndUserId(2L, USER_ID)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(food));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.create(USER_ID, new TransactionRequest(2L, 3L, new BigDecimal("12.5"), TransactionType.EXPENSE, "Tesco", DATE));

        // 12.5 comes back as 12.50, isEqualTo on a BigDecimal checks the decimal places as well as the value.
        assertThat(response.amount()).isEqualTo(new BigDecimal("12.50"));
        assertThat(response.categoryName()).isEqualTo("Food");

    }

    @Test
    void createWithSomeoneElseAccountIsNotFoundAndSavesNothing() {

        // Empty means the account isn't this user's, the repository query checks the user id.
        when(accountRepository.findByIdAndUserId(2L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(USER_ID, expenseRequest(3L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");

        // never means save must not have been called even once
        verify(transactionRepository, never()).save(any());

    }

    @Test
    void createRejectsAnIncomeTransactionInAnExpenseCategory() {

        when(accountRepository.findByIdAndUserId(2L, USER_ID)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(food));

        TransactionRequest incomeInFood = new TransactionRequest(2L, 3L, new BigDecimal("10.00"), TransactionType.INCOME, null, DATE);
        assertThatThrownBy(() -> transactionService.create(USER_ID, incomeInFood))
                .isInstanceOf(BadRequestException.class);
        verify(transactionRepository, never()).save(any());

    }

    @Test
    void updateAlsoChecksTheTypeMatchesTheCategory() {

        // An existing Food expense that someone tries to move into Salary while keeping it as EXPENSE.
        Transaction existing = Transaction.builder()
                .id(9L).account(account).category(food)
                .amount(new BigDecimal("5.00")).type(TransactionType.EXPENSE).date(DATE)
                .build();
        when(transactionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(2L, USER_ID)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(4L, USER_ID)).thenReturn(Optional.of(salary));

        assertThatThrownBy(() -> transactionService.update(USER_ID, 9L, expenseRequest(4L)))
                .isInstanceOf(BadRequestException.class);

        // The transaction wasn't touched or saved.
        assertThat(existing.getCategory()).isSameAs(food);
        verify(transactionRepository, never()).save(any());

    }

    @Test
    void listRejectsBadPagingAndDateRangesWithoutTouchingTheDatabase() {

        // size has to be 100 or less.
        assertThatThrownBy(() -> transactionService.list(USER_ID, null, null, null, null, 0, 101))
                .isInstanceOf(BadRequestException.class);
        // pages can't be negative
        assertThatThrownBy(() -> transactionService.list(USER_ID, null, null, null, null, -1, 20))
                .isInstanceOf(BadRequestException.class);
        // from can't be after to
        assertThatThrownBy(() -> transactionService.list(USER_ID, DATE, DATE.minusDays(1), null, null, 0, 20))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(transactionRepository);
    }

    // A normal 10.00 EXPENSE request for the given category, used by a few tests.
    private TransactionRequest expenseRequest(Long categoryId) {

        return new TransactionRequest(2L, categoryId, new BigDecimal("10.00"), TransactionType.EXPENSE, null, DATE);

    }
}

