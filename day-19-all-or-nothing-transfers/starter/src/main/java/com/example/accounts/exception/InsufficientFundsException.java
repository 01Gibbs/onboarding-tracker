package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 400 Bad Request.
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long id) {
        super("Account " + id + " has insufficient funds");
    }
}
