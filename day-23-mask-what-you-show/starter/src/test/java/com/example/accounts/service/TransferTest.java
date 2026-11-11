package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.exception.InsufficientFundsException;
import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

// The whole app with a real (in-memory) database. Unlike @DataJpaTest, the test itself is NOT wrapped in
// a transaction, so we see exactly what AccountService commits.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:transfers")
@ExtendWith(OutputCaptureExtension.class) // lets a test read what was logged
class TransferTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void aTransferMovesTheMoney() {
        Account from = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));
        Account to = accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("0.00")));

        accountService.transfer(from.getId(), to.getId(), new BigDecimal("40.00"), "ada", false, newKey());

        assertThat(balanceOf(from)).isEqualByComparingTo("60.00");
        assertThat(balanceOf(to)).isEqualByComparingTo("40.00");
    }

    @Test
    void aFailedTransferChangesNothing() {
        Account from = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));

        assertThatThrownBy(() -> accountService.transfer(from.getId(), 999L, new BigDecimal("40.00"), "ada", false, newKey()))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(balanceOf(from)).isEqualByComparingTo("100.00"); // the money must not vanish
    }

    @Test
    void aTransferWithoutEnoughMoneyChangesNothing() {
        Account from = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));
        Account to = accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("0.00")));

        assertThatThrownBy(() -> accountService.transfer(from.getId(), to.getId(), new BigDecimal("500.00"), "ada", false, newKey()))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(balanceOf(from)).isEqualByComparingTo("100.00");
        assertThat(balanceOf(to)).isEqualByComparingTo("0.00");
    }

    @Test
    void theTransferLogNeverShowsAFullAccountNumber(CapturedOutput output) {
        Account from = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00"), "11112222"));
        Account to = accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("0.00"), "33334444"));

        accountService.transfer(from.getId(), to.getId(), new BigDecimal("40.00"), "ada", false, newKey());

        assertThat(output).contains("TRANSFER from=****2222 to=****4444"); // still useful to whoever reads the log...
        assertThat(output).doesNotContain("11112222").doesNotContain("33334444"); // ...without the full numbers
    }

    private static String newKey() {
        return UUID.randomUUID().toString();
    }

    private BigDecimal balanceOf(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getBalance();
    }
}
