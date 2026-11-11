package com.example.accounts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Calls the notifications service (emails and texts to customers) on behalf of the accounts API.
@Component
public class NotificationsClient {

    // The key comes from configuration: in production an environment variable set from the vault,
    // never the code. The old key was in the repository's history, so it was revoked and reissued.
    private final String apiKey;

    public NotificationsClient(@Value("${notifications.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    // The header we send on every call to the notifications service
    public String authorizationHeader() {
        return "Bearer " + apiKey;
    }
}
