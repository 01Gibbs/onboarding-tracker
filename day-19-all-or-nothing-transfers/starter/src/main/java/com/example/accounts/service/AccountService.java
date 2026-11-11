package com.example.accounts.service;

import com.example.accounts.config.AuditLog;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.exception.UnsupportedCurrencyException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private static final Logger audit = LoggerFactory.getLogger("security.audit");

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    // Customers may only see their own accounts. Tellers may see any account.
    public Account getVisibleTo(Long id, String username, boolean isTeller) {
        Account account = getById(id);
        if (!isTeller && !account.getUsername().equals(username)) {
            audit.warn("ACCESS_DENIED user={} account={}", AuditLog.clean(username), id);
            // 404, not 403: don't even confirm that someone else's account exists
            throw new AccountNotFoundException(id);
        }
        return account;
    }

    // The caller's own accounts in one currency, e.g. GET /accounts/mine?currency=GBP
    public List<Account> findMine(String username, String currency) {
        // Spring Data sends username and currency as bound parameters: always data, never part of the query
        return accountRepository.findByUsernameAndCurrency(username, currency);
    }

    // Move money from one account to another
    public void transfer(Long fromId, Long toId, BigDecimal amount, String username, boolean isTeller) {
        Account from = getVisibleTo(fromId, username, isTeller); // customers may only move their own money
        from.withdraw(amount);
        accountRepository.save(from);

        Account to = getById(toId);
        to.deposit(amount);
        accountRepository.save(to);
    }

    public Account create(CreateAccountRequest request) {
        if (!SupportedCurrencies.isSupported(request.currency())) {
            throw new UnsupportedCurrencyException(request.currency());
        }
        Account account = new Account(request.username(), request.owner(), request.currency(), request.openingBalance());
        return accountRepository.save(account);
    }

    public Account updateOwner(Long id, UpdateAccountRequest request) {
        Account account = getById(id);
        account.setOwner(request.owner());
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        accountRepository.deleteById(id);
    }
}
