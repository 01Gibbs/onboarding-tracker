package com.example.accounts.model;

import java.math.BigDecimal;

public record Account(String id, String owner, BigDecimal balance, String currency) {
}
