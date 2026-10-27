package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.PageResponse;
import com.husnain.financetracker.dto.TransactionRequest;
import com.husnain.financetracker.dto.TransactionResponse;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// This controller handles the transaction endpoints, it passes everything to TransactionService.
// Every endpoint in here starts with /api/transactions.
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor

public class TransactionController {

    private final TransactionService transactionService;

    // GET /api/transactions lists the user's transactions, newest first, one page at a time.
    // Every filter is optional, like ?from=2026-10-01&to=2026-10-31&categoryId=3&type=EXPENSE&page0&size=20
    // @RequestParam(required = false) means it can be left out and then it's null so the filter just doesn't get used.
    // @DateTimeFormat tells Spring the dates comes in as YYYY-MM-DD.
    // page and size have defaults so leaving them out gives the first page of 20.
    @GetMapping
    public PageResponse<TransactionResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size ) {

        return transactionService.list(user.getId(), from, to, categoryId, type, page, size);

    }

    // POST /api/transactions make a new transaction, 201 Created if it works.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody TransactionRequest request) {

        return transactionService.create(user.getId(), request);

    }

    // GET /api/transactions/{id} gets one transaction.
    @GetMapping("/{id}")
    public TransactionResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {

        return transactionService.get(user.getId(), id);

    }

    // PUT /api/transacctions/{id} updates a transaction.
    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id, @Valid @RequestBody TransactionRequest request) {

        return transactionService.update(user.getId(), id, request);

    }

    // DELETE /api/transactions/{id} deletes a transaction, 204 No Content if it works.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {

        transactionService.delete(user.getId(), id);

    }
}
