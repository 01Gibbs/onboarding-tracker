package com.example.accounts.config;

// One place that decides how much of a sensitive value we show. Responses and logs both call it,
// so they can't drift apart, and changing the rule (say, last 3 digits) is a one-line change.
public final class Mask {

    private static final int VISIBLE = 4;

    private Mask() {
        // utility class: no instances
    }

    // "12345678" -> "****5678". Enough for a customer to recognise the account, not enough to use it.
    public static String accountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        if (accountNumber.length() <= VISIBLE) {
            return "****"; // too short to show any of it safely
        }
        return "****" + accountNumber.substring(accountNumber.length() - VISIBLE);
    }
}
