package com.example.accounts.dto;

import com.example.accounts.model.Account;
import java.math.BigDecimal;

// What the API sends back. Keeps the database entity out of our public contract.
public record AccountResponse(Long id, String owner, String currency, BigDecimal balance) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(), account.getOwner(), account.getCurrency(), account.getBalance());
    }
}
