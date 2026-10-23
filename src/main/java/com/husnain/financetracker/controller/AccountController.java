package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.AccountRequest;
import com.husnain.financetracker.dto.AccountResponse;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.AccountService;
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

// This controller handles all the account endpoints so listing, creating, getting, updating and deleting accounts.
// It doesn't do any real logic itself, it just takes the request and passes it to AccountService.
// @RestController means whatever these methods return gets sent back as JSON.
// @RequestMapping means every endpoint in here starts with /api/accounts.
// @RequiredArgsConstructor is Lombok making a constructor for the final fields, Spring uses that to pass in the AccountService.

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor

public class AccountController {

    private final AccountService accountService;

    // GET /api/accounts gives back all the accounts for the logged in user.
    // @AuthenticationPrincipal gives us the logged in user from their JWT so we always use their id and never trust an id from the request.
    @GetMapping
    public List<AccountResponse> list(@AuthenticationPrincipal UserPrincipal user) {

        return accountService.list(user.getId());

    }

    // POST /api/accounts makes a new account for the logged in user.
    // @Valid checks the request against the rules in AccountRequest first, so bad data gets rejected before it even gets to the service.
    // @RequestBody turns the JSON body into an AccountRequest.
    // @ResponseStatus makes it return 201 Created instead of the normal 200.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody AccountRequest request) {

        return accountService.create(user.getId(), request);

    }

    // GET /api/accounts/{id} gets one account.
    // @PathVariable grabs the id from the URL.
    // The user id gets passed in too so the service only finds the account if it belongs to this user.
    @GetMapping("/{id}")
    public AccountResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {

        return accountService.get(user.getId(), id);

    }

    // PUT /api/accounts/{id} updates an account with the new details from the body.
    // Same as before, it gets validated first and the user id makes sure people can only edit their own accounts.
    @PutMapping("/{id}")
    public AccountResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id, @Valid @RequestBody AccountRequest request) {

        return accountService.update(user.getId(), id, request);

    }

    // DELETE /api/accounts/{id} deletes an account.
    // It returns 204 No Content because there's nothing to send back after deleting.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {

        accountService.delete(user.getId(), id);

    }
}
