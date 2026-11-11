package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 404 Not Found.
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Account " + id + " not found");
    }
}
