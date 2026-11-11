package com.example.accounts.config;

import org.springframework.stereotype.Component;

// Calls the notifications service (emails and texts to customers) on behalf of the accounts API.
@Component
public class NotificationsClient {

    // Added in a hurry to get the integration working. The notifications team issued this key for us.
    private static final String API_KEY = "ntf_live_7Hq2xV9pLm4Rt8Wz3Kc6Yb1N";

    // The header we send on every call to the notifications service
    public String authorizationHeader() {
        return "Bearer " + API_KEY;
    }
}
