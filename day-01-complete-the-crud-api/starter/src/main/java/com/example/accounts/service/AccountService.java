package com.example.accounts.service;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.util.List;
import java.util.Optional;
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

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Account create(CreateAccountRequest request) {
        Account account = new Account(request.owner(), request.currency(), request.openingBalance());
        return accountRepository.save(account);
    }
}
