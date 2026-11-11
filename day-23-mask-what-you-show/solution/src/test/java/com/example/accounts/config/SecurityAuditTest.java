package com.example.accounts.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.accounts.controller.AccountController;
import com.example.accounts.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Real HTTP Basic logins (no @WithMockUser), so Spring Security really checks the password and publishes events.
@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, SecurityAuditListener.class})
@ExtendWith(OutputCaptureExtension.class) // lets a test read what was logged
class SecurityAuditTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;

    @Test
    void aFailedLoginIsLoggedWithTheUsername(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/accounts/1").with(httpBasic("ada", "wrong-pass")))
                .andExpect(status().isUnauthorized());

        assertThat(output).contains("LOGIN_FAILED user=ada reason=BadCredentialsException");
    }

    @Test
    void thePasswordIsNeverLogged(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/accounts/1").with(httpBasic("ada", "wrong-pass")));

        assertThat(output).doesNotContain("wrong-pass");
    }

    @Test
    void aUsernameCannotAddFakeLogLines(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/accounts/1").with(httpBasic("ada\nLOGIN_OK user=teller", "x")));

        assertThat(output).doesNotContain("\nLOGIN_OK");
    }
}
