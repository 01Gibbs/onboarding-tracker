package com.example.accounts.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.config.AccountsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/** Was loaded from the XML files; now a Boot web slice with the real in-memory service. */
@WebMvcTest(AccountController.class)
@Import(AccountsConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsAllAccounts() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].owner").value("Ada"))
                .andExpect(jsonPath("$[0].currency").value("GBP"));
    }

    @Test
    void getsOneAccount() throws Exception {
        mockMvc.perform(get("/accounts/ACC-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Grace"));
    }

    @Test
    void unknownAccountIs404() throws Exception {
        mockMvc.perform(get("/accounts/ACC-999"))
                .andExpect(status().isNotFound());
    }
}
