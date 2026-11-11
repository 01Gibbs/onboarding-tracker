package com.example.accounts.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

// Plain unit tests (Day 4): no Spring needed, we just call the config's methods.
class PasswordStorageTest {

    SecurityConfig config = new SecurityConfig();
    PasswordEncoder encoder = config.passwordEncoder();

    @Test
    void noUserHasAPlainTextPassword() {
        UserDetailsService users = config.userDetailsService();

        for (String username : new String[] {"ada", "grace", "teller"}) {
            assertThat(users.loadUserByUsername(username).getPassword()).doesNotStartWith("{noop}");
        }
    }

    @Test
    void aHashNamesItsAlgorithmAndHidesThePassword() {
        String hash = encoder.encode("correct horse battery staple");

        assertThat(hash).startsWith("{pbkdf2}").doesNotContain("correct horse");
    }

    @Test
    void theSamePasswordHashesDifferentlyEachTime() {
        // A random salt per hash: two users with the same password get different hashes
        assertThat(encoder.encode("ada-pass")).isNotEqualTo(encoder.encode("ada-pass"));
    }

    @Test
    void matchesChecksAPasswordAgainstAHash() {
        String hash = encoder.encode("ada-pass");

        assertThat(encoder.matches("ada-pass", hash)).isTrue();
        assertThat(encoder.matches("wrong-pass", hash)).isFalse();
    }
}
