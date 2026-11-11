package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.accounts.model.Account;
import com.example.accounts.model.Money;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

// Two requests for the same account at the same moment, replayed step by step so the test fails the same way every time.
// No threads, no sleeps: we nest one transaction inside the other to choose exactly who reads and who commits when.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:concurrency")
class ConcurrentTransferTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void aWithdrawalBasedOnAStaleBalanceIsRejectedNotLost() {
        Long id = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00"))).getId();

        TransactionTemplate first = new TransactionTemplate(transactionManager);
        TransactionTemplate second = new TransactionTemplate(transactionManager);
        second.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // its own transaction, committed on its own

        Throwable thrown = catchThrowable(() -> first.executeWithoutResult(status -> {
            Account seenByFirst = accountRepository.findById(id).orElseThrow();      // 1. first reads 100.00

            second.executeWithoutResult(inner -> {
                Account seenBySecond = accountRepository.findById(id).orElseThrow(); // 2. second also reads 100.00
                seenBySecond.withdraw(new Money(new BigDecimal("30.00"), "GBP"));                      // 3. second takes 30.00 and commits: 70.00
            });

            seenByFirst.withdraw(new Money(new BigDecimal("50.00"), "GBP")); // 4. first takes 50.00 from the 100.00 it read earlier...
        }));                                               // 5. ...and commits 50.00 over the top of 70.00

        assertThat(balanceOf(id))
                .as("30.00 and 50.00 were both withdrawn, but the balance only went down by 50.00: the 30.00 was lost")
                .isEqualByComparingTo("70.00"); // the second commit stands; the stale one must not overwrite it
        assertThat(thrown)
                .as("the commit based on a stale read should fail")
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    private BigDecimal balanceOf(Long id) {
        return accountRepository.findById(id).orElseThrow().getBalance();
    }
}
