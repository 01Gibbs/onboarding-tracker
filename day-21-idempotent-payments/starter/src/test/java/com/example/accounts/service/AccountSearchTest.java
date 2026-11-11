package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

// A real (in-memory) database plus the real AccountService: we're testing the query it runs.
@DataJpaTest
@Import(AccountService.class)
class AccountSearchTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @BeforeEach
    void saveTwoCustomersAccounts() {
        accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("10.00")));
        accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("20.00")));
    }

    @Test
    void findsOnlyTheCallersAccounts() {
        List<Account> found = accountService.findMine("ada", "GBP");

        assertThat(found).extracting(Account::getUsername).containsExactly("ada");
    }

    @Test
    void aQuoteInTheCurrencyCannotWidenTheSearch() {
        // Input that tries to close the quoted string and add a condition that is always true
        List<Account> found = accountService.findMine("ada", "GBP' OR '1'='1");

        assertThat(found).isEmpty();
    }
}
