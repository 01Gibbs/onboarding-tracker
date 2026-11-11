package com.example.accounts.dto;

import com.example.accounts.model.Account;
import java.math.BigDecimal;

public class AccountResponse {

    private final Long id;
    private final String owner;
    private final String currency;
    private final BigDecimal balance;

    public AccountResponse(Long id, String owner, String currency, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
    }

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(), account.getOwner(), account.getCurrency(), account.getBalance());
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
