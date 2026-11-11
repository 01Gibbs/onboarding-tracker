package com.example.accounts.service;

import com.example.accounts.config.AuditLog;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.exception.IdempotencyKeyReusedException;
import com.example.accounts.exception.UnsupportedCurrencyException;
import com.example.accounts.model.Account;
import com.example.accounts.model.IdempotencyRecord;
import com.example.accounts.repository.AccountRepository;
import com.example.accounts.repository.IdempotencyRecordRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final Logger audit = LoggerFactory.getLogger("security.audit");

    private final AccountRepository accountRepository;
    private final IdempotencyRecordRepository idempotencyRecords;

    public AccountService(AccountRepository accountRepository, IdempotencyRecordRepository idempotencyRecords) {
        this.accountRepository = accountRepository;
        this.idempotencyRecords = idempotencyRecords;
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

    // Move money from one account to another, at most once per idempotency key. Returns the HTTP status to answer with.
    // All or nothing: the key is saved in the SAME transaction as the transfer. If the transfer fails, the key isn't
    // saved either, so the client can retry; if the key can't be saved, the money doesn't move.
    @Transactional
    public int transfer(Long fromId, Long toId, BigDecimal amount, String username, boolean isTeller, String idempotencyKey) {
        String fingerprint = fingerprint(fromId, toId, amount);

        Optional<IdempotencyRecord> earlier = idempotencyRecords.findByUsernameAndIdempotencyKey(username, idempotencyKey);
        if (earlier.isPresent()) {
            if (!earlier.get().getFingerprint().equals(fingerprint)) {
                throw new IdempotencyKeyReusedException(); // same key, different payment: a client bug, not a retry
            }
            return earlier.get().getResponseStatus(); // a retry: same answer as last time, and no second transfer
        }

        // Fail closed: check everything BEFORE changing anything
        Account from = getVisibleTo(fromId, username, isTeller); // customers may only move their own money
        Account to = getById(toId);

        from.withdraw(amount);
        to.deposit(amount);
        accountRepository.save(from);
        accountRepository.save(to);

        int status = HttpStatus.NO_CONTENT.value();
        idempotencyRecords.save(new IdempotencyRecord(idempotencyKey, username, fingerprint, status));
        return status;
    }

    // What was asked for, written so that 50, 50.0 and 50.00 count as the same request
    private static String fingerprint(Long fromId, Long toId, BigDecimal amount) {
        return "from=" + fromId + " to=" + toId + " amount=" + amount.stripTrailingZeros().toPlainString();
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
