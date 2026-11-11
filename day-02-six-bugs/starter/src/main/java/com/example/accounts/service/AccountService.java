package com.example.accounts.service;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account getById(Long id) {
        return accountRepository.findById(id).get();
    }

    public Account doStuff(CreateAccountRequest r) {
        String c = r.currency();
        if (!(c == "GBP" || c == "EUR" || c == "USD")) {
            throw new IllegalArgumentException("Unsupported currency: " + c);
        }
        Account a = new Account(r.owner(), c, new BigDecimal(r.openingBalance()));
        return accountRepository.save(a);
    }

    public Account updateOwner(Long id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
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
