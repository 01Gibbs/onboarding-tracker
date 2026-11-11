package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// SAST-102: tokens are hashed with SHA-256, not MD5. Plain unit tests: no Spring needed.
class TokenHashingTest {

    TokenHasher hasher = new TokenHasher();

    @Test
    void tokensAreHashedWithSha256() {
        // The SHA-256 test vector for "abc" from FIPS 180-2. MD5 gives 900150983cd24fb0d6963f7d28e17f72.
        assertThat(hasher.hash("abc")).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void aStoredHashMatchesOnlyItsOwnToken() {
        String stored = hasher.hash("partner-token-1");

        assertThat(hasher.matches("partner-token-1", stored)).isTrue();
        assertThat(hasher.matches("partner-token-2", stored)).isFalse();
    }
}
