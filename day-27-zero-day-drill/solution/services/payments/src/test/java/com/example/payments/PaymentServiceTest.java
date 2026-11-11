package com.example.payments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    PaymentService payments = new PaymentService();

    @Test
    void aPositivePaymentIsMade() {
        assertDoesNotThrow(() -> payments.pay("ada", "acc-1", "acc-2", 5000));
    }

    @Test
    void aPaymentMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> payments.pay("ada", "acc-1", "acc-2", 0));
    }
}
