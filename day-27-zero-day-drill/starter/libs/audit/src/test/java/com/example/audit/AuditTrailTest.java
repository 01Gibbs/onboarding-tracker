package com.example.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuditTrailTest {

    @Test
    void aLineSaysWhoDidWhatToWhat() {
        assertEquals("AUDIT user=ada action=PAY target=acc-2", AuditTrail.line("ada", "PAY", "acc-2"));
    }

    @Test
    void aValueCannotAddAFakeLine() {
        assertEquals("AUDIT user=ada_AUDIT user=admin action=PAY target=acc-2",
                AuditTrail.line("ada\nAUDIT user=admin", "PAY", "acc-2"));
    }
}
