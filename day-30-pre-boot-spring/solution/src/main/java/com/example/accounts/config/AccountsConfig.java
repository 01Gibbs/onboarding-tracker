package com.example.accounts.config;

import com.example.accounts.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Was WEB-INF/applicationContext.xml. */
@Configuration
public class AccountsConfig {

    // Was <bean id="accountService"> with <constructor-arg value="${accounts.currency}"/>.
    // Boot reads application.properties itself, so the PropertyPlaceholderConfigurer bean is gone.
    @Bean
    AccountService accountService(@Value("${accounts.currency}") String currency) {
        return new AccountService(currency);
    }
}
