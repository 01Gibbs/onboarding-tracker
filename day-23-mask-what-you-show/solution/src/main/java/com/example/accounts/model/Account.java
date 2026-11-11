package com.example.accounts.model;

import com.example.accounts.exception.InsufficientFundsException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.security.SecureRandom;

@Entity
public class Account {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String accountNumber; // the 8-digit number printed on statements, e.g. "12345678"

    @Column(nullable = false)
    private String username; // the login that owns this account, e.g. "ada"

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // Optimistic locking: Hibernate adds 1 on every update and only writes if nobody else changed it first:
    //   UPDATE account SET balance = ?, version = 4 WHERE id = ? AND version = 3
    // If no row matches, someone else got there first: the update fails instead of silently overwriting theirs.
    @Version
    private Long version;

    // JPA needs a no-argument constructor. protected = "not for you, for JPA".
    protected Account() {
    }

    // A new account gets a random 8-digit number
    public Account(String username, String owner, String currency, BigDecimal balance) {
        this(username, owner, currency, balance, "%08d".formatted(RANDOM.nextInt(100_000_000)));
    }

    public Account(String username, String owner, String currency, BigDecimal balance, String accountNumber) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // Money checks the currencies match: you can't put dollars into a pound account
    public void deposit(Money amount) {
        balance = currentBalance().add(amount).amount();
    }

    public void withdraw(Money amount) {
        Money after = currentBalance().subtract(amount);
        if (after.isNegative()) {
            throw new InsufficientFundsException(id);
        }
        balance = after.amount();
    }

    private Money currentBalance() {
        return new Money(balance, currency);
    }
}
