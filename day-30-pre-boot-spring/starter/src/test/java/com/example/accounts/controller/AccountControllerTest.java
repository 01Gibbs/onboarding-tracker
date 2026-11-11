package com.example.accounts.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Loads the same XML bean definitions the application server loads from web.xml. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration // a web context, with src/main/webapp as the web app root
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/dispatcher-servlet.xml"
})
class AccountControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

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
