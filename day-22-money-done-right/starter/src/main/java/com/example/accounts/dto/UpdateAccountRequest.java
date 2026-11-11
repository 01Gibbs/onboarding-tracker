package com.example.accounts.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAccountRequest(@NotBlank String owner) {
}
