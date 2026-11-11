package com.example.accounts.service;

import com.example.accounts.model.Account;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Accounts held in memory. Defined as a bean in config/AccountsConfig. */
public class AccountService {

    private final Map<String, Account> accounts = new TreeMap<>();

    public AccountService(String currency) {
        add(new Account("ACC-1", "Ada", new BigDecimal("1200.00"), currency));
        add(new Account("ACC-2", "Grace", new BigDecimal("300.00"), currency));
    }

    public List<Account> findAll() {
        return List.copyOf(accounts.values());
    }

    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    private void add(Account account) {
        accounts.put(account.id(), account);
    }
}
