package com.example.accounts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String username,                           // the customer's login, e.g. "ada"
        @NotBlank String owner,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance) {
}
