package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 400 Bad Request.
public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(String currency) {
        super("Unsupported currency: " + currency);
    }
}
