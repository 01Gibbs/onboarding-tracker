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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// The whole app (real security, real service, in-memory database), called the way a client calls it: over HTTP.
// A mobile app on a bad connection sends a payment, never sees the answer, and sends it again. The key says "same payment".
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:idempotency")
@AutoConfigureMockMvc
class IdempotencyTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    Account adas;
    Account graces;

    @BeforeEach
    void twoGbpAccounts() {
        adas = accountRepository.save(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("100.00")));
        graces = accountRepository.save(new Account("grace", "Grace Hopper", "GBP", new BigDecimal("0.00")));
    }

    @Test
    void theSameKeyTwiceMovesTheMoneyOnce() throws Exception {
        String key = UUID.randomUUID().toString();

        transfer("40.00", key).andExpect(status().isNoContent());
        transfer("40.00", key).andExpect(status().isNoContent()); // the retry gets the same answer...

        assertThat(balanceOf(adas)).isEqualByComparingTo("60.00"); // ...but the money moved only once
        assertThat(balanceOf(graces)).isEqualByComparingTo("40.00");
    }

    @Test
    void theSameKeyWithADifferentBodyIs422() throws Exception {
        String key = UUID.randomUUID().toString();
        transfer("40.00", key).andExpect(status().isNoContent());

        transfer("45.00", key)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Idempotency key reused"));

        assertThat(balanceOf(adas)).isEqualByComparingTo("60.00");
    }

    @Test
    void aTransferWithoutAKeyIs400() throws Exception {
        transfer("40.00", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Idempotency key required"));

        assertThat(balanceOf(adas)).isEqualByComparingTo("100.00");
    }

    // TODO: keysAreScopedPerUser — Ada and the teller each send a transfer from Ada's account with the SAME key: both 204, the money moves twice

    private ResultActions transfer(String amount, String idempotencyKey) throws Exception {
        MockHttpServletRequestBuilder request = post("/accounts/" + adas.getId() + "/transfers")
                .with(httpBasic("ada", "ada-pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"toAccountId": %d, "amount": %s}
                        """.formatted(graces.getId(), amount));
        if (idempotencyKey != null) {
            request.header("Idempotency-Key", idempotencyKey);
        }
        return mockMvc.perform(request);
    }

    private BigDecimal balanceOf(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getBalance();
    }
}
