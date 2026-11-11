package com.example.accounts.controller;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // CREATE — POST /accounts
    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody CreateAccountRequest request) {
        Account created = accountService.create(request);
        return ResponseEntity
                .created(URI.create("/accounts/" + created.getId())) // 201 + Location header
                .body(created);
    }

    @GetMapping
    public List<Account> getAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        // ResponseEntity.of turns an empty Optional into a 404 Not Found.
        return ResponseEntity.of(accountService.findById(id));
    }
}
