package com.example.accounts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank String owner,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @DecimalMin("0.00") double openingBalance) {
}
