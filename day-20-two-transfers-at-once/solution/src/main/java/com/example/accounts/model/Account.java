package com.example.accounts.model;

import com.example.accounts.exception.InsufficientFundsException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public Account(String username, String owner, String currency, BigDecimal balance) {
        this.username = username;
        this.owner = owner;
        this.currency = currency;
        this.balance = balance;
    }

    public Long getId() {
        return id;
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

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id);
        }
        balance = balance.subtract(amount);
    }
}
