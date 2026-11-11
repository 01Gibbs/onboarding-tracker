package com.example.accounts.service;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

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

    public Account create(CreateAccountRequest request) {
        Account account = new Account(request.getOwner(), request.getCurrency(), request.getOpeningBalance());
        return accountRepository.save(account);
    }
}
