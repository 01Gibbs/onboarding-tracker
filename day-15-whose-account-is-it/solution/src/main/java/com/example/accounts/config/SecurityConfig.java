package com.example.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Checked top to bottom: the first matching rule wins.
                        .requestMatchers("/hello").permitAll()                                           // public on purpose
                        .requestMatchers(HttpMethod.GET, "/accounts/{id}").hasAnyRole("CUSTOMER", "TELLER") // read one account
                        .requestMatchers("/accounts/**").hasRole("TELLER")                              // everything else on accounts: staff only
                        .anyRequest().authenticated())                                                  // anything else: log in first
                .httpBasic(Customizer.withDefaults())                            // username + password on every request
                // A JSON API called with HTTP Basic, not a browser with a session cookie: no session, no CSRF token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // ⚠️ Plain-text passwords ({noop}) keep Day 14 small. Day 17 replaces them with hashes.
        return new InMemoryUserDetailsManager(
                User.withUsername("ada").password("{noop}ada-pass").roles("CUSTOMER").build(),
                User.withUsername("grace").password("{noop}grace-pass").roles("CUSTOMER").build(),
                User.withUsername("teller").password("{noop}teller-pass").roles("TELLER").build());
    }
}
