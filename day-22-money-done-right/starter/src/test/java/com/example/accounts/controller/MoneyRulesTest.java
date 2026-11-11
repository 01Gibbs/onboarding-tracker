package com.example.accounts.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.model.Account;
import com.example.accounts.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// The whole app over HTTP, like IdempotencyTest. Two ways money goes wrong without anyone noticing.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:money")
@AutoConfigureMockMvc
class MoneyRulesTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void aTransferBetweenCurrenciesIs400AndChangesNothing() throws Exception {
        Account pounds = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));
        Account dollars = accountRepository.save(new Account("grace", "Grace Hopper", "USD", new BigDecimal("0.00")));

        transfer(pounds, dollars, "40.00") // 40 pounds must not arrive as 40 dollars
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Currency mismatch"));

        assertThat(balanceOf(pounds)).isEqualByComparingTo("100.00");
        assertThat(balanceOf(dollars)).isEqualByComparingTo("0.00");
    }

    @Test
    void anAmountWithMoreThanTwoDecimalPlacesIs400() throws Exception {
        Account from = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));
        Account to = accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("0.00")));

        transfer(from, to, "10.005") // half a penny: which way should it round? Better not to accept it at all
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.amount").exists());

        assertThat(balanceOf(from)).isEqualByComparingTo("100.00");
        assertThat(balanceOf(to)).isEqualByComparingTo("0.00");
    }

    private ResultActions transfer(Account from, Account to, String amount) throws Exception {
        return mockMvc.perform(post("/accounts/" + from.getId() + "/transfers")
                .with(httpBasic("ada", "ada-pass"))
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"toAccountId": %d, "amount": %s}
                        """.formatted(to.getId(), amount)));
    }

    private BigDecimal balanceOf(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getBalance();
    }
}
