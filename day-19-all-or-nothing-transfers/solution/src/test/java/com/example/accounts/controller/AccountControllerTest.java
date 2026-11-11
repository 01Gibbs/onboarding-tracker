package com.example.accounts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.config.SecurityConfig;
import com.example.accounts.exception.AccountNotFoundException;
import com.example.accounts.model.Account;
import com.example.accounts.service.AccountService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Starts only the web layer (controllers, JSON, validation), not the database.
@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)   // use OUR security rules, not Spring Boot's defaults
@WithMockUser(roles = "TELLER") // every test runs as a logged-in teller, unless it says otherwise
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc; // sends fake HTTP requests to our controller

    @MockitoBean
    AccountService accountService; // the controller gets this fake instead of the real service

    @Test
    @WithAnonymousUser // overrides the class-level @WithMockUser: nobody is logged in
    void anonymousCallersAreRejected() throws Exception {
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCanReadAnAccount() throws Exception {
        when(accountService.getVisibleTo(1L, "user", false)) // @WithMockUser's default username is "user"
                .thenReturn(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("1250.00")));

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCannotListEveryAccount() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(accountService);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCannotDeleteAccounts() throws Exception {
        mockMvc.perform(delete("/accounts/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(accountService);
    }

    @Test
    void getByIdReturnsTheAccountAsJson() throws Exception {
        when(accountService.getVisibleTo(1L, "user", true))
                .thenReturn(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("1250.00")));

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Ada Lovelace"))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }

    @Test
    void getByIdReturns404WhenTheAccountIsMissing() throws Exception {
        when(accountService.getVisibleTo(99L, "user", true)).thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get("/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Account not found")); // from ApiExceptionHandler
    }

    @Test
    void createReturns201() throws Exception {
        when(accountService.create(any()))
                .thenReturn(new Account("ada", "Ada Lovelace", "GBP", new BigDecimal("10.00")));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ada", "owner": "Ada Lovelace", "currency": "GBP", "openingBalance": 10.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value("Ada Lovelace"));
    }

    @Test
    void createRejectsABlankOwner() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ada", "owner": "", "currency": "GBP", "openingBalance": 10.00}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService); // validation stopped it before the service was called
    }
}
