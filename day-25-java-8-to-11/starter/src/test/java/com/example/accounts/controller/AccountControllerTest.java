package com.example.accounts.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.example.accounts.export.XmlExporter;
import com.example.accounts.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
@Import({AccountService.class, XmlExporter.class})
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void listReturnsTheAccountsAsJson() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owner").value("Ada Lovelace"));
    }

    @Test
    void exportReturnsTheAccountsAsXml() throws Exception {
        mockMvc.perform(get("/accounts/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("count(/accounts/account)").number(2.0))
                .andExpect(xpath("/accounts/account[@id='1']/owner").string("Ada Lovelace"));
    }
}
