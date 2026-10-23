package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//This is what the client sends when making or updating an account.
//It's a record, so Java makes the constructor and getters for us and the values can't be changed after it's made.
//The annotations are the validation rules, they only run because the controller has @Valid on the request body.
//If any rule fails the request gets rejected and never reaches the service.

public record AccountRequest(

        // The account name, like the name of your bank account.
        // @Schema example is just the example value Swagger shows in the docs.
        // @NotBlank means it can't be null, empty or just spaces and @Size means it can't be longer than 100 characters.
        @Schema(example = "Monzo Current")
        @NotBlank @Size(max = 100)
        String name,

        // What kind of account it is, this has to be one of the values in the AccountType enum.
        // @NotNull means they have to send it.
        @NotNull
        AccountType type,

        // The currency code for the account, like GBP, USD or EUR.
        // The regex means it has to be exactly 3 capital letters, nothing more and nothing less.
        // It doesn't check if it's a real currency though so something like ABC would still get through.
        @Schema(example = "GBP")
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be 3 capital letters, e.g. GBP")
        String currency

) {
}
