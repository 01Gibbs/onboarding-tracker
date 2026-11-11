package com.example.accounts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Starts only the web layer (controllers, JSON, validation), not the database.
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc; // sends fake HTTP requests to our controller

    @MockitoBean
    AccountService accountService; // the controller gets this fake instead of the real service

    // TODO: customersCanReadAnAccount — a CUSTOMER gets 200 from GET /accounts/1
    // TODO: customersCannotListEveryAccount — a CUSTOMER gets 403 from GET /accounts
    // TODO: customersCannotDeleteAccounts — a CUSTOMER gets 403 from DELETE /accounts/1
    // TODO: anonymousCallersAreRejected — GET /accounts/1 without logging in is 401, and the service is never called

    @Test
    void getByIdReturnsTheAccountAsJson() throws Exception {
        when(accountService.getById(1L))
                .thenReturn(new Account("Ada Lovelace", "GBP", new BigDecimal("1250.00")));

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Ada Lovelace"))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }

    @Test
    void getByIdReturns404WhenTheAccountIsMissing() throws Exception {
        when(accountService.getById(99L)).thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get("/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Account not found")); // from ApiExceptionHandler
    }

    @Test
    void createReturns201() throws Exception {
        when(accountService.create(any()))
                .thenReturn(new Account("Ada Lovelace", "GBP", new BigDecimal("10.00")));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"owner": "Ada Lovelace", "currency": "GBP", "openingBalance": 10.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("Ada Lovelace"));
    }

    @Test
    void createRejectsABlankOwner() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"owner": "", "currency": "GBP", "openingBalance": 10.00}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService); // validation stopped it before the service was called
    }
}
