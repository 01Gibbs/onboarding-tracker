package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 422 Unprocessable Content.
public class IdempotencyKeyReusedException extends RuntimeException {

    public IdempotencyKeyReusedException() {
        super("This Idempotency-Key was already used for a different request. Use a new key for a new payment");
    }
}
