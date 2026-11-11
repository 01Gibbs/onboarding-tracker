package com.example.accounts.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long toAccountId,
        // Pounds and pence: at most 2 decimal places. 17 + 2 = 19 digits, the size of the balance column.
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount) {
}
