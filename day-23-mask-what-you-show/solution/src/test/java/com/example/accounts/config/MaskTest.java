package com.example.accounts.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// Plain unit tests (Day 4) for your masking helper: no Spring needed.
class MaskTest {

    @Test
    void showsOnlyTheLastFourDigits() {
        assertThat(Mask.accountNumber("12345678")).isEqualTo("****5678");
    }

    @Test
    void hidesAShortNumberCompletely() {
        assertThat(Mask.accountNumber("123")).isEqualTo("****");
    }

    @Test
    void nullStaysNull() {
        assertThat(Mask.accountNumber(null)).isNull();
    }
}
