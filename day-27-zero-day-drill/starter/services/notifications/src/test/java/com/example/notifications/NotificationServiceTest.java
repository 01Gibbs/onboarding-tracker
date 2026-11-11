package com.example.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    @Test
    void theMessageShowsPounds() {
        assertEquals("Hello grace, you received 50.00", new NotificationService().paymentReceived("grace", 5000));
    }
}
