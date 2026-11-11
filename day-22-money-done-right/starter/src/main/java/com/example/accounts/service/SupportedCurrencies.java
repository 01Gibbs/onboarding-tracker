package com.example.accounts.service;

import java.util.Set;

public final class SupportedCurrencies {

    private static final Set<String> CODES = Set.of("GBP", "EUR", "USD");

    private SupportedCurrencies() {
        // utility class: no instances
    }

    public static boolean isSupported(String currency) {
        return currency != null && CODES.contains(currency);
    }
}
