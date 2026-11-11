package com.example.notifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Sends emails and texts to customers.
public class NotificationService {

    private static final Logger LOG = LogManager.getLogger(NotificationService.class);

    public String paymentReceived(String user, long amountPence) {
        String message = "Hello " + user + ", you received " + amountPence / 100 + "." + String.format("%02d", amountPence % 100);
        LOG.info("Notification queued for user={}", user);
        return message;
    }
}
