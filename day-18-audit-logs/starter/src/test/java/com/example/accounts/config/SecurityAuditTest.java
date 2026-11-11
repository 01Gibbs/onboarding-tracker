package com.example.accounts.config;

import com.example.accounts.controller.AccountController;
import com.example.accounts.service.AccountService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Real HTTP Basic logins (no @WithMockUser), so Spring Security really checks the password and publishes events.
@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class) // TODO: also import your SecurityAuditListener once you've written it
@ExtendWith(OutputCaptureExtension.class) // lets a test read what was logged
class SecurityAuditTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;

    // TODO: aFailedLoginIsLoggedWithTheUsername — "LOGIN_FAILED user=ada reason=BadCredentialsException"
    // TODO: thePasswordIsNeverLogged — the password the caller tried never appears
    // TODO: aUsernameCannotAddFakeLogLines — a line break in the username can't start a new log line
}
