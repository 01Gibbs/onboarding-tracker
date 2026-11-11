package com.example.accounts.config;

public final class AuditLog {

    private AuditLog() {
        // utility class: no instances
    }

    // The username comes from the caller. Replace line breaks so nobody can forge an extra log line.
    public static String clean(String value) {
        return value == null ? null : value.replace('\r', '_').replace('\n', '_');
    }
}
