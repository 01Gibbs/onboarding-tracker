package com.example.accounts.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccountService accountService;

    @Test
    void anonymousCallersAreRejected() throws Exception {
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getByIdReturnsTheAccount() throws Exception {
        when(accountService.getById(1L))
                .thenReturn(new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00")));

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Ada Lovelace"));
    }

    @Test
    @WithMockUser
    void missingAccountIs404() throws Exception {
        when(accountService.getById(99L)).thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get("/accounts/99"))
                .andExpect(status().isNotFound());
    }
}
