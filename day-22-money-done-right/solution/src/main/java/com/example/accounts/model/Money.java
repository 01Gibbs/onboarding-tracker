package com.example.accounts.model;

import com.example.accounts.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// An amount AND its currency, always together. A bare BigDecimal can't stop you adding pounds to dollars; this can.
// A record: immutable, so add() and subtract() return a new Money and never change this one.
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        // Always two decimal places, so 10.5 and 10.50 are equal Money. UNNECESSARY means "never round":
        // 10.005 throws instead of quietly becoming 10.00 or 10.01. Where rounding is part of a rule
        // (interest, currency conversion), name the mode there, e.g. RoundingMode.HALF_EVEN.
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }
}
