package com.example.accounts.exception;

// ApiExceptionHandler turns this into a 400 Bad Request.
public class IdempotencyKeyRequiredException extends RuntimeException {

    public IdempotencyKeyRequiredException() {
        super("Send an Idempotency-Key header (for example a new UUID, at most 64 characters) with every transfer, and the same key when you retry it");
    }
}
