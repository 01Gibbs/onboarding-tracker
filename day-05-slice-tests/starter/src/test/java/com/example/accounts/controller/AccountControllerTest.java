package com.example.accounts.controller;

import com.example.accounts.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;

    // TODO: getByIdReturnsTheAccountAsJson — GET /accounts/1 is 200 with $.owner and $.currency
    // TODO: getByIdReturns404WhenTheAccountIsMissing — 404, and $.title is "Account not found"
    // TODO: createReturns201 — POST /accounts with valid JSON is 201
    // TODO: createRejectsABlankOwner — 400, and the service is never called
}
