package com.example.accounts.dto;

import com.example.accounts.config.Mask;
import com.example.accounts.model.Account;
import java.math.BigDecimal;

// What the API sends back. Keeps the database entity out of our public contract,
// and is the one place every account leaves the API, so masking here covers every endpoint.
public record AccountResponse(Long id, String accountNumber, String owner, String currency, BigDecimal balance) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                Mask.accountNumber(account.getAccountNumber()), // "****5678": never the full number
                account.getOwner(), account.getCurrency(), account.getBalance());
    }
}
