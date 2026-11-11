package com.example.accounts.controller;

import com.example.accounts.dto.AccountResponse;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.dto.UpdateAccountRequest;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account created = accountService.create(request);
        return ResponseEntity
                .created(URI.create("/accounts/" + created.getId())) // 201 + Location header
                .body(AccountResponse.from(created));
    }

    // READ — GET /accounts
    @GetMapping
    public List<AccountResponse> getAll() {
        List<AccountResponse> responses = new ArrayList<>();
        for (Account account : accountService.findAll()) {
            responses.add(AccountResponse.from(account));
        }
        return responses;
    }

    // READ — GET /accounts/mine?currency=GBP: the caller's own accounts in one currency
    @GetMapping("/mine")
    public List<AccountResponse> getMine(@RequestParam String currency, Authentication caller) {
        List<AccountResponse> responses = new ArrayList<>();
        for (Account account : accountService.findMine(caller.getName(), currency)) {
            responses.add(AccountResponse.from(account));
        }
        return responses;
    }

    // READ — GET /accounts/{id}
    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id, Authentication caller) { // Spring passes in who's logged in
        return AccountResponse.from(accountService.getVisibleTo(id, caller.getName(), isTeller(caller)));
    }

    // UPDATE — PUT /accounts/{id}
    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest request) {
        return AccountResponse.from(accountService.updateOwner(id, request));
    }

    // DELETE — DELETE /accounts/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204: success, nothing to send back
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }

    private static boolean isTeller(Authentication caller) {
        return caller.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TELLER"));
    }
}
