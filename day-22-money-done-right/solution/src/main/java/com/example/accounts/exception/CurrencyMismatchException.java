package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 400 Bad Request.
public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String expected, String actual) {
        super("Cannot combine " + expected + " with " + actual + ": both accounts must use the same currency");
    }
}
