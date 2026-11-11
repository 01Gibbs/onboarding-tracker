package com.example.reporting;

import com.example.audit.AuditTrail;
import java.util.List;

// Turns a list of transactions into the text of a statement.
public final class StatementFormatter {

    public record Line(String date, String description, long amountPence) {
    }

    public String format(String user, String accountId, List<Line> lines) {
        StringBuilder text = new StringBuilder("Statement for " + accountId + "\n");
        long total = 0;
        for (Line line : lines) {
            text.append(line.date()).append("  ").append(line.description()).append("  ")
                    .append(pounds(line.amountPence())).append("\n");
            total += line.amountPence();
        }
        text.append("Total  ").append(pounds(total)).append("\n");
        AuditTrail.record(user, "STATEMENT_GENERATED", accountId);
        return text.toString();
    }

    static String pounds(long pence) {
        return (pence < 0 ? "-" : "") + Math.abs(pence) / 100 + "." + String.format("%02d", Math.abs(pence) % 100);
    }
}
