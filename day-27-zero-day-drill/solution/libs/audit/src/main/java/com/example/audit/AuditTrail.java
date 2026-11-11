package com.example.audit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Writes one audit line per action: who did what, to what.
public final class AuditTrail {

    private static final Logger LOG = LogManager.getLogger("audit");

    private AuditTrail() {
        // utility class: no instances
    }

    public static void record(String user, String action, String target) {
        LOG.info(line(user, action, target));
    }

    // Line breaks in a value are replaced, so nobody can forge an extra audit line (Day 18)
    static String line(String user, String action, String target) {
        return "AUDIT user=" + clean(user) + " action=" + clean(action) + " target=" + clean(target);
    }

    private static String clean(String value) {
        return value == null ? "-" : value.replace('\r', '_').replace('\n', '_');
    }
}
