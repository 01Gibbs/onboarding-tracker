package com.example.reporting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class StatementFormatterTest {

    @Test
    void penceArePrintedAsPounds() {
        assertEquals("12.05", StatementFormatter.pounds(1205));
        assertEquals("-0.50", StatementFormatter.pounds(-50));
    }

    @Test
    void aStatementEndsWithItsTotal() {
        String text = new StatementFormatter().format("ada", "acc-1", List.of(
                new StatementFormatter.Line("2026-11-01", "Salary", 250000),
                new StatementFormatter.Line("2026-11-02", "Rent", -120000)));

        assertEquals("Total  1300.00", text.lines().reduce((first, last) -> last).orElseThrow());
    }
}
