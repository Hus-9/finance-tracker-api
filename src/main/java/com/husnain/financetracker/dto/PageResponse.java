package com.husnain.financetracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// This is what the API sends back for anything that comes in page, like the transaction list.
// content is the items on this page, page is which page it is (starting at 0) and size is how many fit on a page
// totalElements is how many there are altogether and totalPages is how many pages that makes.
// The <T> means it works for any type so PageResponse<TransactionResponse> is a page of transactions.
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    // Turns Spring's Page into our own simple version so the JSON always looks the same.
    public static <T> PageResponse<T> from (Page<T> page) {

        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());

    }
}
