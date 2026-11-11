package com.example.statements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatementServiceTest {

    @Test
    void aStatementNamesItsAccount() {
        assertTrue(new StatementService().monthlyStatement("ada", "acc-1").startsWith("Statement for acc-1"));
    }
}
