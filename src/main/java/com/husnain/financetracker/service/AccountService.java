package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.AccountRequest;
import com.husnain.financetracker.dto.AccountResponse;
import com.husnain.financetracker.exception.ResourceNotFoundException;
import com.husnain.financetracker.model.Account;
import com.husnain.financetracker.repository.AccountRepository;
import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// This is where the actual logic for  accounts happens, the AccountController just passes everything in here.
// Every method takes the logged in user's id so a user can only ever see or change their own accounts.
// @Service makes it a Spring bean so it can be passed into the controller.
@Service
@RequiredArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // Gets all of the user's accounts sorted by name and turns each one into an AccountResponse.
    public List<AccountResponse> list(Long userId) {

        return accountRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(AccountResponse::from)
                .toList();

    }

    // Gets one account, findOwnedAccount throws a 404 if it doesn't exist or isn't theirs.
    public AccountResponse get(Long userId, Long accountId) {

        return AccountResponse.from(findOwnedAccount(userId, accountId));

    }

    // Makes a new account for the user.
    // @Transactional means everything in here happens as one database transaction so it either all saves or none of it does.
    @Transactional
    public AccountResponse create(Long userId, AccountRequest request) {

        // getReferenceById doesn't actually load the user from the database, it just makes a placeholder with the id.
        // That's all we need to set user_id on the account so it saves a query.
        // It's safe because the id comes from a valid token so we know the user exists.
        Account account = Account.builder()
                .user(userRepository.getReferenceById(userId))
                .name(request.name())
                .type(request.type())
                .currency(request.currency())
                .build();
        // We return what save gives back because that's when the id and createdAt get filled in.
        return AccountResponse.from(accountRepository.save(account));

    }

    // Updates an account with the new details.
    // It finds the account first so it's a 404 if it isn't theirs, then it changes the fields and saves.
    @Transactional
    public AccountResponse update(Long userId, Long accountId, AccountRequest request) {

        Account account = findOwnedAccount(userId, accountId);
        account.setName(request.name());
        account.setType(request.type());
        account.setCurrency(request.currency());
        // Inside a transaction Hibernate would save the changes by itself anyway, calling save just makes it obvious.
        return AccountResponse.from(accountRepository.save(account));

    }

    // Deletes an account, again it checks it's theirs first.
    // The database is set to cascade so deleting an account also deletes all the transactions in it.
    @Transactional
    public void delete(Long userId, Long accountId) {

        accountRepository.delete(findOwnedAccount(userId, accountId));

    }

    // This is used by get, update and delete to find an account that belongs to this user.
    // If it doesn't exist or belongs to someone else it throws ResourceNotFoundException which becomes a 404.
    private Account findOwnedAccount(Long userId, Long accountId) {

        return accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    }
}
