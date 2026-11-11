package com.example.payments;

import com.example.audit.AuditTrail;

// Moves money between accounts, and records every payment in the audit trail.
public class PaymentService {

    public void pay(String user, String fromAccount, String toAccount, long amountPence) {
        if (amountPence <= 0) {
            throw new IllegalArgumentException("A payment must be for a positive amount");
        }
        // ... the real service would move the money here ...
        AuditTrail.record(user, "PAY " + amountPence, fromAccount + "->" + toAccount);
    }
}
