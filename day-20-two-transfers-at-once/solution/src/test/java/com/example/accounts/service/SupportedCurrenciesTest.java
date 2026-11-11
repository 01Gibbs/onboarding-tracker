package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupportedCurrenciesTest {

    @Test
    void gbpIsSupported() {
        assertThat(SupportedCurrencies.isSupported("GBP")).isTrue();
    }

    @Test
    void unknownCurrencyIsNotSupported() {
        assertThat(SupportedCurrencies.isSupported("XYZ")).isFalse();
    }

    @Test
    void lowercaseCodeIsNotSupported() {
        assertThat(SupportedCurrencies.isSupported("gbp")).isFalse();
    }

    @Test
    void nullIsNotSupported() {
        assertThat(SupportedCurrencies.isSupported(null)).isFalse();
    }
}
