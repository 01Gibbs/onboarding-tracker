package com.example.accounts.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.model.Account;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// Starts only JPA + a fresh in-memory H2 database. Each test is rolled back afterwards.
@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    @Test
    void startsEmpty() {
        assertThat(accountRepository.count()).isZero();
    }

    @Test
    void savesAndFindsAnAccount() {
        Account saved = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("10.50")));

        assertThat(saved.getId()).isNotNull();

        Optional<Account> found = accountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOwner()).isEqualTo("Ada Lovelace");
        assertThat(found.get().getBalance()).isEqualByComparingTo("10.50");
    }
}
