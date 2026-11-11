package com.example.accounts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// One row per payment we've already carried out: "this user, this key, this request, this answer".
// The unique constraint is the last line of defence: the database refuses a second row for the same user and key.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"username", "idempotency_key"}))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 64) // "key" alone is a reserved word in SQL
    private String idempotencyKey;

    @Column(nullable = false)
    private String username; // keys are per user: Ada's "abc" and Grace's "abc" are different payments

    @Column(nullable = false)
    private String fingerprint; // what was asked for, so we can spot the same key sent with a different request

    @Column(nullable = false)
    private int responseStatus; // what we answered, so a retry gets the same answer

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String username, String fingerprint, int responseStatus) {
        this.idempotencyKey = idempotencyKey;
        this.username = username;
        this.fingerprint = fingerprint;
        this.responseStatus = responseStatus;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public int getResponseStatus() {
        return responseStatus;
    }
}
