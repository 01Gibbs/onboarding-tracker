package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupportedCurrenciesTest {

    @Test
    void gbpIsSupported() {
        assertThat(SupportedCurrencies.isSupported("GBP")).isTrue();
    }

    // TODO: unknownCurrencyIsNotSupported — "XYZ" is not supported
    // TODO: lowercaseCodeIsNotSupported — "gbp" is not supported
    // TODO: nullIsNotSupported — null is not supported
}
