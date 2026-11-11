package com.example.accounts.controller;

import com.example.accounts.dto.AccountResponse;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
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

    @GetMapping
    public List<AccountResponse> getAll() {
        return accountService.findAll().stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id) {
        return AccountResponse.from(accountService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account created = accountService.create(request);
        return ResponseEntity
                .created(URI.create("/accounts/" + created.getId()))
                .body(AccountResponse.from(created));
    }
}
