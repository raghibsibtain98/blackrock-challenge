package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;

public record InvalidTransactionFilter(
        String date,
        BigDecimal amount,
        String message) {}
