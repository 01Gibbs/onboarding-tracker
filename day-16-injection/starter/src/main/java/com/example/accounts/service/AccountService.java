package com.example.accounts.service;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.exception.UnsupportedCurrencyException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    public AccountService(AccountRepository accountRepository, EntityManager entityManager) {
        this.accountRepository = accountRepository;
        this.entityManager = entityManager;
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
            // 404, not 403: don't even confirm that someone else's account exists
            throw new AccountNotFoundException(id);
        }
        return account;
    }

    // The caller's own accounts in one currency, e.g. GET /accounts/mine?currency=GBP
    public List<Account> findMine(String username, String currency) {
        String jpql = "SELECT a FROM Account a WHERE a.username = '" + username
                + "' AND a.currency = '" + currency + "'";
        return entityManager.createQuery(jpql, Account.class).getResultList();
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
