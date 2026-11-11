package com.example.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
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
                        .requestMatchers(HttpMethod.POST, "/accounts/{id}/transfers").hasAnyRole("CUSTOMER", "TELLER")
                        .requestMatchers("/accounts/**").hasRole("TELLER")                              // everything else on accounts: staff only
                        .anyRequest().authenticated())                                                  // anything else: log in first
                .httpBasic(Customizer.withDefaults())                            // username + password on every request
                // A JSON API called with HTTP Basic, not a browser with a session cookie: no session, no CSRF token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // New hashes use PBKDF2. The {id} prefix on each stored hash names the algorithm that made it,
        // so we can move to a stronger one later without breaking the passwords already stored.
        return new DelegatingPasswordEncoder("pbkdf2", Map.of(
                "pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Demo users for local development only. We store salted PBKDF2 hashes, never the passwords.
        // Real services get users from an identity provider, and must never ship with default logins.
        return new InMemoryUserDetailsManager(
                User.withUsername("ada")
                        .password("{pbkdf2}e257dbe3347d021d4e7f61b23d4a3b9f4b601c9c0f617263a210c226c8c05ec781a31f80610f7ec526dc437bb08eca4f")
                        .roles("CUSTOMER").build(),
                User.withUsername("grace")
                        .password("{pbkdf2}17b81076f85c3a4e39cde858e3030f8e804ae0b15207b9f23513feb7511fcedeef2c892c4ad748a3ab95ce5264be6da8")
                        .roles("CUSTOMER").build(),
                User.withUsername("teller")
                        .password("{pbkdf2}c53f946f79e709670dcea012e08352b2b02cd2de4b6eb4c3f71f56fe2f9294c56943ba1e136013230b76d035a31f6f20")
                        .roles("TELLER").build());
    }
}
