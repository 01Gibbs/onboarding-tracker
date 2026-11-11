package com.example.statements;

import com.example.reporting.StatementFormatter;
import java.util.List;

// Produces monthly statements. The formatting lives in the shared reporting library.
public class StatementService {

    private final StatementFormatter formatter = new StatementFormatter();

    public String monthlyStatement(String user, String accountId) {
        // ... the real service would load the month's transactions here ...
        List<StatementFormatter.Line> lines = List.of(
                new StatementFormatter.Line("2026-11-01", "Salary", 250000),
                new StatementFormatter.Line("2026-11-02", "Rent", -120000));
        return formatter.format(user, accountId, lines);
    }
}
