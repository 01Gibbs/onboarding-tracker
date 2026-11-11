package com.example.accounts.config;

import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Runs once at startup. Inserts sample data only if the table is empty.
@Component
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;

    public DataSeeder(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            accountRepository.save(new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00")));
            accountRepository.save(new Account("Grace Hopper", "USD", new BigDecimal("980.50")));
        }
    }
}
