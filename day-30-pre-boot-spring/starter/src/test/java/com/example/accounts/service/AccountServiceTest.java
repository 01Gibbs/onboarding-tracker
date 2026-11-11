package com.example.accounts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccountServiceTest {

    private final AccountService service = new AccountService("GBP");

    @Test
    void startsWithTwoAccountsInTheGivenCurrency() {
        assertEquals(2, service.findAll().size());
        assertTrue(service.findAll().stream().allMatch(a -> a.currency().equals("GBP")));
    }

    @Test
    void unknownIdIsEmpty() {
        assertTrue(service.findById("ACC-999").isEmpty());
    }
}
