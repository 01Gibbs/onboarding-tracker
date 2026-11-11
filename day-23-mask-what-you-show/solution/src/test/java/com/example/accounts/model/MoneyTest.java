package com.example.accounts.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.accounts.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

// Plain unit tests (Day 4) for your Money type: no Spring, no database.
class MoneyTest {

    @Test
    void addingTheSameCurrencyAddsTheAmounts() {
        assertThat(gbp("10.00").add(gbp("2.50"))).isEqualTo(gbp("12.50"));
    }

    @Test
    void addingADifferentCurrencyIsRefused() {
        assertThatThrownBy(() -> gbp("10.00").add(new Money(new BigDecimal("1.00"), "USD")))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void subtractingADifferentCurrencyIsRefused() {
        assertThatThrownBy(() -> gbp("10.00").subtract(new Money(new BigDecimal("1.00"), "USD")))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void amountsAlwaysHaveTwoDecimalPlaces() {
        assertThat(gbp("10.5").amount()).hasToString("10.50");
        assertThat(gbp("10.5")).isEqualTo(gbp("10.50")); // BigDecimal's own equals says 10.5 != 10.50
    }

    @Test
    void moreThanTwoDecimalPlacesIsRefusedNotRounded() {
        assertThatThrownBy(() -> gbp("10.005"))
                .isInstanceOf(ArithmeticException.class);
    }

    private static Money gbp(String amount) {
        return new Money(new BigDecimal(amount), "GBP");
    }
}
