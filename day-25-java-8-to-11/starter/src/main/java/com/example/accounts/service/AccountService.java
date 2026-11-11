package com.example.accounts.service;

import com.example.accounts.model.Account;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/** Accounts held in memory: enough for this service's one job, the nightly export. */
@Service
public class AccountService {

    private final List<Account> accounts = new ArrayList<>(Arrays.asList(
            new Account(1L, "Ada Lovelace", "GBP", new BigDecimal("1250.00")),
            new Account(2L, "Grace Hopper", "USD", new BigDecimal("980.50"))));

    public List<Account> findAll() {
        return Collections.unmodifiableList(accounts);
    }
}
