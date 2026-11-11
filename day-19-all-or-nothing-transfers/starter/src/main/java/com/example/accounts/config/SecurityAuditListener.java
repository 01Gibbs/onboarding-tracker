package com.example.accounts.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

// Spring Security publishes an event for every failed login. We write each one to an audit log.
@Component
public class SecurityAuditListener {

    // A named logger, so operations can route audit lines somewhere tamper-resistant and alert on them
    private static final Logger audit = LoggerFactory.getLogger("security.audit");

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent event) {
        // Log WHO and WHY. Never log the password the caller tried, even a wrong one.
        audit.warn("LOGIN_FAILED user={} reason={}",
                AuditLog.clean(event.getAuthentication().getName()),
                event.getException().getClass().getSimpleName());
    }
}
