package com.example.accounts.controller;

import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    // READ — GET /accounts
    @GetMapping
    public List<Account> getAll() {
        return accountService.findAll();
    }

    // READ — GET /accounts/{id}
    @GetMapping("/{id}")
    public Account getById(@PathVariable Long id) {
        return accountService.getById(id);
    }

    // UPDATE — PUT /accounts/{id}
    @PutMapping("/{id}")
    public Account update(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateOwner(id, request);
    }

    // DELETE — DELETE /accounts/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204: success, nothing to send back
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }
}
