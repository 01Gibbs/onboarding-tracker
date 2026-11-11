package com.example.accounts.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

// API tokens for partner integrations. We store only a hash of each token,
// so a copy of the database doesn't hand out working tokens.
// Tokens are long random values, so one fast SHA-256 is enough. Passwords are not: they use the
// slow, salted PasswordEncoder from Day 17.
@Component
public class TokenHasher {

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Compare a token someone presents with the hash we stored
    public boolean matches(String token, String storedHash) {
        return MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
